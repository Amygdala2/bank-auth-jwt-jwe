package com.judith126.bank.auth.service;

import com.judith126.bank.auth.dto.AuthResponse;
import com.judith126.bank.auth.dto.LoginRequest;
import com.judith126.bank.auth.dto.RegisterRequest;
import com.judith126.bank.auth.dto.UserProfileResponse;
import com.judith126.bank.auth.entity.UserAccount;
import com.judith126.bank.auth.repository.UserRepository;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        UserAccount user = new UserAccount(
                request.username(),
                passwordEncoder.encode(request.password()),
                "CLIENT"
        );
        userRepository.save(user);
        TokenService.IssuedTokens tokens = tokenService.issueTokens(user);
        return AuthResponse.of(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        TokenService.IssuedTokens tokens = tokenService.issueTokens(user);
        return AuthResponse.of(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }

    public AuthResponse refresh(String refreshToken) {
        JWTClaimsSet claims = tokenService.parseRefreshToken(refreshToken);
        UserAccount user = userRepository.findByUsername(claims.getSubject())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        TokenService.IssuedTokens tokens = tokenService.issueTokens(user);
        return AuthResponse.of(tokens.accessToken(), tokens.refreshToken(), tokens.expiresInSeconds());
    }

    public UserProfileResponse profile(String username) {
        UserAccount user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new UserProfileResponse(user.getUsername(), user.getRole());
    }
}
