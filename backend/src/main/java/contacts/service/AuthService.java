package contacts.service;

import contacts.dto.AuthResponse;
import contacts.dto.UserDto;
import contacts.model.AuthProvider;
import contacts.model.User;
import contacts.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

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
        return buildAuthResponse(user);
    }

    private User createUser(String email, String name) {
        Instant now = Instant.now();
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProvider(AuthProvider.GOOGLE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String jwt = jwtService.generateToken(user);
        return new AuthResponse(jwt, UserDto.from(user));
    }
}
