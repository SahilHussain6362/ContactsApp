package contacts.controller;

import contacts.dto.AuthResponse;
import contacts.dto.ForgotPasswordSendOtpRequest;
import contacts.dto.GoogleAuthRequest;
import contacts.dto.LoginRequest;
import contacts.dto.RegisterSendOtpRequest;
import contacts.dto.RegisterVerifyRequest;
import contacts.dto.ResetPasswordRequest;
import contacts.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/register/send-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void registerSendOtp(@RequestBody @Valid RegisterSendOtpRequest req) {
        authService.registerSendOtp(req.getEmail());
    }

    @PostMapping("/register/verify-otp")
    public AuthResponse registerVerify(@RequestBody @Valid RegisterVerifyRequest req) {
        return authService.registerVerify(req.getEmail(), req.getOtp(), req.getPassword(), req.getName());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req.getEmail(), req.getPassword());
    }

    @PostMapping("/forgot-password/send-otp")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPasswordSendOtp(@RequestBody @Valid ForgotPasswordSendOtpRequest req) {
        authService.forgotPasswordSendOtp(req.getEmail());
    }

    @PostMapping("/forgot-password/verify-otp")
    public AuthResponse resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        return authService.resetPassword(req.getEmail(), req.getOtp(), req.getNewPassword());
    }
}
