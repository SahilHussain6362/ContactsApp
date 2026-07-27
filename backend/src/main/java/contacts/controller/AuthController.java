package contacts.controller;

import contacts.dto.AuthResponse;
import contacts.dto.GoogleAuthRequest;
import contacts.dto.UpdateProfileRequest;
import contacts.dto.UserDto;
import contacts.security.CurrentUser;
import contacts.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/google")
    public AuthResponse google(@RequestBody @Valid GoogleAuthRequest req) {
        return authService.googleLogin(req.getIdToken());
    }


    // The client refreshes this on every sync: it carries the current display name and the
    // authoritative bookmark set, neither of which travels with the contact documents.
    @GetMapping("/me")
    public UserDto me() {
        return authService.getProfile(currentUser.require());
    }


    @PatchMapping("/me")
    public UserDto updateMe(@RequestBody @Valid UpdateProfileRequest req) {
        return authService.updateProfile(currentUser.require(), req.getName());
    }
}
