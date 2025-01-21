package org.example.service;

import org.example.config.CryptographyConfig;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Logger;


public class CryptographyService {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final CryptographyConfig config = new CryptographyConfig();
    private static final Random secureRandom = new SecureRandom();
    private static final Logger LOGGER = Logger.getLogger(CryptographyService.class.getName());

    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(config.FACTORY_INSTANCE);
        KeySpec spec = new PBEKeySpec(password, salt, config.PBKDF2_ITERATIONS, config.PBKDF2_KEY_LENGTH);
        // Clear the password array
        java.util.Arrays.fill(password, '\0');

        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
    public static String encrypt(String password, String plainMessage) throws Exception {
        char[] passwordChars = password.toCharArray();
        byte[] salt = generateRandomSalt(config.AES_SALT_LENGTH);
        SecretKey secretKey = getAESKeyFromPassword(passwordChars, salt);

        // Clear the password array
        java.util.Arrays.fill(passwordChars, '\0');

        byte[] iv = getRandomNonce(config.AES_NONCE_LENGTH);

        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encryptedMessageByte = cipher.doFinal(plainMessage.getBytes(UTF_8));

        byte[] cipherByte = ByteBuffer.allocate(salt.length + iv.length + encryptedMessageByte.length)
                .put(salt)
                .put(iv)
                .put(encryptedMessageByte)
                .array();
        return Base64.getEncoder().encodeToString(cipherByte);
    }

    public static String decrypt(String cipherContent, String password) throws Exception {
        byte[] decode = Base64.getDecoder().decode(cipherContent.getBytes(UTF_8));
        ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

        byte[] salt = new byte[config.AES_SALT_LENGTH];
        byteBuffer.get(salt);

        byte[] iv = new byte[config.AES_NONCE_LENGTH];
        byteBuffer.get(iv);

        byte[] content = new byte[byteBuffer.remaining()];
        byteBuffer.get(content);

        char[] passwordChars = password.toCharArray();
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(passwordChars, salt);
        java.util.Arrays.fill(passwordChars, '\0');

        Cipher cipher = initCipher(Cipher.DECRYPT_MODE, aesKeyFromPassword, iv);
        byte[] plainText = cipher.doFinal(content);
        return new String(plainText, UTF_8);
    }

    public static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        secureRandom.nextBytes(nonce);
        return nonce;
    }

    private static byte[] generateRandomSalt(int length) {
        return getRandomNonce(length);
    }

    private static Cipher initCipher(int mode, SecretKey secretKey, byte[] nonce) throws
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(config.CIPHER_ALGORITHM);
        cipher.init(mode, secretKey, new GCMParameterSpec(config.AES_TAG_LENGTH_BITS, nonce));
        return cipher;
    }
}