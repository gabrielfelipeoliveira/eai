package com.eai.infrastructure.email;

import com.eai.application.email.EncryptionService;
import com.eai.infrastructure.config.EmailCredentialEncryptionProperties;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AesGcmEmailCredentialEncryptionService implements EncryptionService {

    private static final String VERSION_PREFIX = "v1:";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES = 12;

    private final SecretKeySpec keySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmEmailCredentialEncryptionService(EmailCredentialEncryptionProperties properties) {
        this.keySpec = new SecretKeySpec(sha256(properties.effectiveSecret()), "AES");
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("password required");
        }
        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return VERSION_PREFIX + base64(iv) + ":" + base64(cipherText);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not encrypt email credential", exception);
        }
    }

    @Override
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isBlank()) {
            throw new IllegalArgumentException("encrypted password required");
        }
        if (!encryptedText.startsWith(VERSION_PREFIX)) {
            return decryptLegacyBase64(encryptedText);
        }

        String payload = encryptedText.substring(VERSION_PREFIX.length());
        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted email credential format");
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (AEADBadTagException exception) {
            throw new IllegalArgumentException("Invalid encrypted email credential", exception);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalArgumentException("Could not decrypt email credential", exception);
        }
    }

    private String decryptLegacyBase64(String encryptedText) {
        try {
            return new String(Base64.getDecoder().decode(encryptedText), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid legacy email credential format", exception);
        }
    }

    private static byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Could not initialize email credential encryption key", exception);
        }
    }

    private static String base64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
