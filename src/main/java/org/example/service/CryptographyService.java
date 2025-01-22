package org.example.service;

import org.example.config.CryptographyConfig;

import javax.crypto.*;
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
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;


public class CryptographyService {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final CryptographyConfig config = new CryptographyConfig();
    private static final Logger LOGGER = Logger.getLogger(CryptographyService.class.getName());
    private static final ThreadLocal<SecureRandom> secureRandom = ThreadLocal.withInitial(SecureRandom::new);


    /**
     * Generates an AES key from the given password and salt.
     *
     * @param password the password to derive the key from
     * @param salt     the salt to use for key derivation
     * @return the generated AES key
     * @throws RuntimeException if key extraction fails
     */
    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(config.FACTORY_INSTANCE);
            KeySpec spec = new PBEKeySpec(password, salt, config.PBKDF2_ITERATIONS, config.PBKDF2_KEY_LENGTH);
            // Clear the password array
            java.util.Arrays.fill(password, '\0');

            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(SEVERE, "AES key extraction failed:", e);
            throw new RuntimeException();
        }
    }

    /**
     * Encrypts a plain message using the given password.
     *
     * @param password     the password to derive the encryption key from
     * @param plainMessage the message to encrypt
     * @return the encrypted message in Base64 encoding
     * @throws RuntimeException if encryption fails
     */
    public static String encrypt(String password, String plainMessage) {
        try {
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
        } catch (RuntimeException e) {
            LOGGER.log(SEVERE, "Encryption failed:", e);
            return null;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(SEVERE, "Encryption failed:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Decrypts an encrypted message using the given password.
     *
     * @param cipherContent the encrypted message in Base64 encoding
     * @param password      the password to derive the decryption key from
     * @return the decrypted plain message
     * @throws RuntimeException if decryption fails
     */
    public static String decrypt(String cipherContent, String password) {
        try {
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
        } catch (AEADBadTagException e) {
            LOGGER.log(SEVERE, "Decryption failed due to wrong TAG/password:", e);
            return null;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(SEVERE, "Decryption failed due:", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a random nonce of the specified length.
     *
     * @param length the length of the nonce
     * @return the generated nonce
     */
    public static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        secureRandom.get().nextBytes(nonce);
        return nonce;
    }

    /**
     * Generates a random salt of the specified length.
     *
     * @param length the length of the salt
     * @return the generated salt
     */
    private static byte[] generateRandomSalt(int length) {
        return getRandomNonce(length);
    }

    /**
     * Initializes a Cipher instance for encryption or decryption.
     *
     * @param mode      the operation mode of the cipher (Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE)
     * @param secretKey the secret key to use for the cipher
     * @param nonce     the nonce to use for the cipher
     * @return the initialized Cipher instance
     * @throws RuntimeException if cipher initialization fails
     */
    private static Cipher initCipher(int mode, SecretKey secretKey, byte[] nonce) {
        try {
            Cipher cipher = Cipher.getInstance(config.CIPHER_ALGORITHM);
            cipher.init(mode, secretKey, new GCMParameterSpec(config.AES_TAG_LENGTH_BITS, nonce));
            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            LOGGER.log(SEVERE, "Cipher initiation failed:", e);
            throw new RuntimeException();
        } catch (InvalidKeyException e) {
            LOGGER.log(SEVERE, "Invalid key provided:", e);
            throw new RuntimeException();
        }
    }
}