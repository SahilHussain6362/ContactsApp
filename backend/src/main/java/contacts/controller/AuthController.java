package contacts.controller;

import contacts.dto.AuthResponse;
import contacts.dto.GoogleAuthRequest;
import contacts.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/google")
    public AuthResponse google(@RequestBody @Valid GoogleAuthRequest req) {
        return authService.googleLogin(req.getIdToken());
    }
}
