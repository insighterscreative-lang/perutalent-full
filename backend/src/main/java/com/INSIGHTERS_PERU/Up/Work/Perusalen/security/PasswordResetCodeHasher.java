package com.INSIGHTERS_PERU.Up.Work.Perusalen.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetCodeHasher {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String MESSAGE_PREFIX = "PERUTALENT_PASSWORD_RESET";

    private final byte[] secret;

    public PasswordResetCodeHasher(@Value("${password-reset.secret}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("La clave password-reset.secret no puede estar vacía");
        }

        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String hash(String emailNormalizado, String codigo) {
        String mensaje = MESSAGE_PREFIX + "|" + emailNormalizado + "|" + codigo;

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(mensaje.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo proteger el código de recuperación", e);
        }
    }

    public boolean matches(String emailNormalizado, String codigo, String hashGuardado) {
        if (hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }

        byte[] hashCalculado = hash(emailNormalizado, codigo).getBytes(StandardCharsets.UTF_8);
        byte[] hashEsperado = hashGuardado.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(hashCalculado, hashEsperado);
    }
}
