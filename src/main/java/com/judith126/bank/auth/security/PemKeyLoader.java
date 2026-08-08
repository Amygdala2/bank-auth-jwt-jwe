package com.judith126.bank.auth.security;

import com.judith126.bank.auth.config.AuthProperties;
import com.nimbusds.jose.jwk.RSAKey;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemKeyLoader {

    private PemKeyLoader() {
    }

    public static RSAPrivateKey loadPrivateKey(AuthProperties properties) {
        return parsePrivateKey(properties.jwePrivateKeyPem());
    }

    public static RSAPublicKey loadPublicKey(AuthProperties properties) {
        return parsePublicKey(properties.jwePublicKeyPem());
    }

    static RSAPrivateKey parsePrivateKey(String pem) {
        try {
            byte[] decoded = decodePem(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) factory.generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load JWE private key", ex);
        }
    }

    static RSAPublicKey parsePublicKey(String pem) {
        try {
            byte[] decoded = decodePem(pem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load JWE public key", ex);
        }
    }

    public static RSAKey toRsaJwk(RSAPublicKey publicKey) {
        return new RSAKey.Builder(publicKey).build();
    }

    private static byte[] decodePem(String pem) {
        String normalized = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(normalized);
    }
}
