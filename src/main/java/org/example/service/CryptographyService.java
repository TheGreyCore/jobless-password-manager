package org.example.service;

import org.example.config.CryptographyConfig;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;
import java.util.logging.Logger;


public class CryptographyService {
    private static final CryptographyConfig config = new CryptographyConfig();
    private static final Random secureRandom = new SecureRandom();
    private static final Logger LOGGER = Logger.getLogger(CryptographyService.class.getName());

    public SecretKey getAESKeyFromPassword(String plainPassword) {
        try {
            char[] password = plainPassword.toCharArray();

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password, generateRandomSalt(), config.PBKDF2_ITERATIONS,
                    config.PBKDF2_KEY_LENGTH);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.severe(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static byte[] generateRandomSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }
}
