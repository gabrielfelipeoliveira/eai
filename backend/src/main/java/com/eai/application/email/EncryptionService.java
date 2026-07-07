package com.eai.application.email;

public interface EncryptionService {

    String encrypt(String plainText);

    String decrypt(String encryptedText);
}
