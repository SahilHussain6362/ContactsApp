package contacts.service;

import contacts.dto.AuthResponse;
import contacts.dto.UserDto;
import contacts.model.AuthProvider;
import contacts.model.User;
import contacts.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;

    public AuthService(UserRepository userRepository,
                        JwtService jwtService,
                        GoogleTokenService googleTokenService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.googleTokenService = googleTokenService;
    }

    public AuthResponse googleLogin(String idToken) {
        GoogleTokenService.GooglePayload payload = googleTokenService.verify(idToken);
        User user = userRepository.findByEmail(payload.email())
                .orElseGet(() -> createUser(payload.email(), payload.name()));
        log.info("Google login succeeded for user {} ({})", user.getId(), user.getEmail());
        return buildAuthResponse(user);
    }

    public UserDto getProfile(User user) {
        return UserDto.from(user);
    }

    /**
     * Renames the caller. Google's own name is only used to seed the record at first login,
     * so a rename here survives every later login — {@link #googleLogin} looks the user up by
     * email and never writes the name back over an existing document.
     */
    public UserDto updateProfile(User user, String name) {
        log.info("User {} updated profile name to '{}'", user.getId(), name.trim());
        user.setName(name.trim());
        user.setUpdatedAt(Instant.now());
        return UserDto.from(userRepository.save(user));
    }

    private User createUser(String email, String name) {
        Instant now = Instant.now();
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User saved = userRepository.save(user);
        log.info("New user registered via Google: {} ({})", saved.getId(), email);
        return saved;
    }

    private AuthResponse buildAuthResponse(User user) {
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, UserDto.from(user));
    }
}
