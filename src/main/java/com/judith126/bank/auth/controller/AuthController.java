package com.judith126.bank.auth.controller;

import com.judith126.bank.auth.dto.AuthResponse;
import com.judith126.bank.auth.dto.LoginRequest;
import com.judith126.bank.auth.dto.RefreshRequest;
import com.judith126.bank.auth.dto.RegisterRequest;
import com.judith126.bank.auth.dto.UserProfileResponse;
import com.judith126.bank.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.profile(authentication.getName());
    }
}
