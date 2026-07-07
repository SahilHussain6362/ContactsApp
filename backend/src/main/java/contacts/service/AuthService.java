package contacts.service;

import contacts.dto.AuthResponse;
import contacts.dto.UserDto;
import contacts.model.AuthProvider;
import contacts.model.EmailOtp;
import contacts.model.OtpPurpose;
import contacts.model.User;
import contacts.repository.EmailOtpRepository;
import contacts.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final long RESEND_COOLDOWN_SECONDS = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final EmailOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;
    private final OtpMailSender otpMailSender;
    private final long otpExpiryMinutes;
    private final int otpMaxAttempts;

    public AuthService(UserRepository userRepository,
                        EmailOtpRepository otpRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService,
                        GoogleTokenService googleTokenService,
                        OtpMailSender otpMailSender,
                        @Value("${app.otp.expiry-minutes}") long otpExpiryMinutes,
                        @Value("${app.otp.max-attempts}") int otpMaxAttempts) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenService = googleTokenService;
        this.otpMailSender = otpMailSender;
        this.otpExpiryMinutes = otpExpiryMinutes;
        this.otpMaxAttempts = otpMaxAttempts;
    }

    public AuthResponse googleLogin(String idToken) {
        GoogleTokenService.GooglePayload payload = googleTokenService.verify(idToken);
        User user = userRepository.findByEmail(payload.email())
                .orElseGet(() -> createUser(payload.email(), payload.name(), AuthProvider.GOOGLE));
        return buildAuthResponse(user);
    }

    // Only blocks on an existing password — a Google-only account can still complete
    // registration later to add password login for the same email.
    public void registerSendOtp(String email) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            if (existing.getPasswordHash() != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered. Please log in.");
            }
        });
        issueOtp(email, OtpPurpose.REGISTER);
    }

    public AuthResponse registerVerify(String email, String otp, String password, String name) {
        consumeOtp(email, otp, OtpPurpose.REGISTER);

        Instant now = Instant.now();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setCreatedAt(now);
            return u;
        });
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setProvider(AuthProvider.EMAIL_PASSWORD);
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        user.setUpdatedAt(now);
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return buildAuthResponse(user);
    }

    public void forgotPasswordSendOtp(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPasswordHash() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No account registered with this email");
        }
        issueOtp(email, OtpPurpose.RESET);
    }

    public AuthResponse resetPassword(String email, String otp, String newPassword) {
        consumeOtp(email, otp, OtpPurpose.RESET);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No account registered with this email"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(Instant.now());
        user = userRepository.save(user);

        return buildAuthResponse(user);
    }

    private User createUser(String email, String name, AuthProvider provider) {
        Instant now = Instant.now();
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProvider(provider);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private void issueOtp(String email, OtpPurpose purpose) {
        Instant now = Instant.now();
        otpRepository.findByEmailAndPurpose(email, purpose).ifPresent(existing -> {
            if (existing.getExpiresAt().isAfter(now)
                    && existing.getCreatedAt().isAfter(now.minusSeconds(RESEND_COOLDOWN_SECONDS))) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait before requesting another OTP");
            }
        });
        otpRepository.deleteByEmailAndPurpose(email, purpose);

        String otp = generateOtp();
        EmailOtp record = new EmailOtp();
        record.setEmail(email);
        record.setPurpose(purpose);
        record.setOtpHash(passwordEncoder.encode(otp));
        record.setExpiresAt(now.plus(otpExpiryMinutes, ChronoUnit.MINUTES));
        record.setAttemptCount(0);
        record.setCreatedAt(now);
        otpRepository.save(record);

        otpMailSender.send(email, otp);
    }

    private void consumeOtp(String email, String otp, OtpPurpose purpose) {
        EmailOtp record = otpRepository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP not requested or already used"));

        if (record.getExpiresAt().isBefore(Instant.now())) {
            otpRepository.deleteByEmailAndPurpose(email, purpose);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP has expired");
        }
        if (record.getAttemptCount() >= otpMaxAttempts) {
            otpRepository.deleteByEmailAndPurpose(email, purpose);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Too many incorrect attempts, please request a new OTP");
        }
        if (!passwordEncoder.matches(otp, record.getOtpHash())) {
            record.setAttemptCount(record.getAttemptCount() + 1);
            otpRepository.save(record);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid OTP");
        }

        otpRepository.deleteByEmailAndPurpose(email, purpose);
    }

    private AuthResponse buildAuthResponse(User user) {
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, UserDto.from(user));
    }

    private static String generateOtp() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }
}
