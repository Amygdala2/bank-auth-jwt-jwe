package com.judith126.bank.auth.service;

import com.judith126.bank.auth.config.AuthProperties;
import com.judith126.bank.auth.entity.UserAccount;
import com.judith126.bank.auth.security.PemKeyLoader;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    private final AuthProperties authProperties;
    private final RSADecrypter rsaDecrypter;
    private final RSAEncrypter rsaEncrypter;
    private final byte[] jwtSecret;

    public TokenService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.jwtSecret = authProperties.jwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        this.rsaDecrypter = new RSADecrypter(PemKeyLoader.loadPrivateKey(authProperties));
        this.rsaEncrypter = new RSAEncrypter(PemKeyLoader.loadPublicKey(authProperties));
    }

    public IssuedTokens issueTokens(UserAccount user) {
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new IssuedTokens(accessToken, refreshToken, authProperties.accessTokenMinutes() * 60);
    }

    public String createAccessToken(UserAccount user) {
        try {
            Instant expiresAt = Instant.now().plusSeconds(authProperties.accessTokenMinutes() * 60);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .claim("roles", user.getRole())
                    .expirationTime(Date.from(expiresAt))
                    .build();

            SignedJWT signedJwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            signedJwt.sign(new MACSigner(jwtSecret));
            return signedJwt.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Failed to create access token", ex);
        }
    }

    public String createRefreshToken(UserAccount user) {
        try {
            Instant expiresAt = Instant.now().plusSeconds(authProperties.refreshTokenDays() * 24 * 60 * 60);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .jwtID(UUID.randomUUID().toString())
                    .expirationTime(Date.from(expiresAt))
                    .build();

            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM).build(),
                    new com.nimbusds.jose.Payload(claims.toJSONObject())
            );
            jweObject.encrypt(rsaEncrypter);
            return jweObject.serialize();
        } catch (JOSEException ex) {
            throw new IllegalStateException("Failed to create refresh token", ex);
        }
    }

    public JWTClaimsSet parseAccessToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(new MACVerifier(jwtSecret))) {
                throw new InvalidTokenException("Invalid access token signature");
            }
            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date())) {
                throw new InvalidTokenException("Access token expired");
            }
            return claims;
        } catch (ParseException | JOSEException ex) {
            throw new InvalidTokenException("Invalid access token", ex);
        }
    }

    public JWTClaimsSet parseRefreshToken(String token) {
        try {
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(rsaDecrypter);
            JWTClaimsSet claims = JWTClaimsSet.parse(jweObject.getPayload().toJSONObject());
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date())) {
                throw new InvalidTokenException("Refresh token expired");
            }
            return claims;
        } catch (ParseException | JOSEException ex) {
            throw new InvalidTokenException("Invalid refresh token", ex);
        }
    }

    public record IssuedTokens(String accessToken, String refreshToken, long expiresInSeconds) {
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message) {
            super(message);
        }

        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
