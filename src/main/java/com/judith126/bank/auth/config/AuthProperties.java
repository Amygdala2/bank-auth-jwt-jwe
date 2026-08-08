package com.judith126.bank.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        String jwtSecret,
        String jwePrivateKeyPem,
        String jwePublicKeyPem,
        long accessTokenMinutes,
        long refreshTokenDays
) {
    public AuthProperties {
        if (accessTokenMinutes <= 0) {
            accessTokenMinutes = 15;
        }
        if (refreshTokenDays <= 0) {
            refreshTokenDays = 7;
        }
    }
}
