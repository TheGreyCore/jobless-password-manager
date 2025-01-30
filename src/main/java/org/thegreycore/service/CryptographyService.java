package org.thegreycore.service;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.thegreycore.config.CryptographyConfig;

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
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;


public class CryptographyService {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final CryptographyConfig config = new CryptographyConfig();
    private static final Logger LOGGER = Logger.getLogger(CryptographyService.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();


    /**
     * Generates an AES key from the given password and salt.
     *
     * @param password the password to derive the key from
     * @param salt     the salt to use for key derivation
     * @return the generated AES key
     * @throws RuntimeException if key extraction fails
     */
    private static SecretKey getAESKeyFromPassword(char[] password, byte[] salt) {
        if (salt.length != config.AES_SALT_LENGTH) throw new IllegalArgumentException();

        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(config.ARGON2_ITERATIONS)
                .withMemoryAsKB(config.ARGON2_MEMOMORY_LIMIT)
                .withParallelism(config.ARGON2_PARALLELISM)
                .withSalt(salt);

        Argon2BytesGenerator generate = new Argon2BytesGenerator();
        generate.init(builder.build());
        byte[] result = new byte[config.ARGON2_AES_KEY_LENGTH];
        generate.generateBytes(password, result, 0, result.length);
        Arrays.fill(result, (byte) 0);

        return new SecretKeySpec(result, "AES");
    }

    /**
     * Encrypts a plain message using the given masterKey.
     *
     * @param masterKey     the masterKey to derive the encryption key from
     * @param plainMessage the message to encrypt
     * @return the encrypted message in Base64 encoding
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(char[] masterKey, String plainMessage) {
        try {
            byte[] salt = generateRandomSalt(config.AES_SALT_LENGTH);
            SecretKey secretKey = getAESKeyFromPassword(masterKey, salt);

            byte[] randomNonce = getRandomNonce(config.AES_NONCE_LENGTH);

            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, randomNonce);
            assert cipher != null;
            byte[] encryptedMessageByte = cipher.doFinal(plainMessage.getBytes(UTF_8));

            byte[] cipherByte = ByteBuffer.allocate(salt.length + randomNonce.length + encryptedMessageByte.length)
                    .put(salt)
                    .put(randomNonce)
                    .put(encryptedMessageByte)
                    .array();
            return Base64.getEncoder().encodeToString(cipherByte);
        } catch (RuntimeException e) {
            LOGGER.log(SEVERE, "Encryption failed");
            return null;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(SEVERE, "Encryption failed");
        }
        return null;
    }

    /**
     * Decrypts an encrypted message using the given masterKey.
     *
     * @param cipherContent the encrypted message in Base64 encoding
     * @param masterKey      the masterKey to derive the decryption key from
     * @return the decrypted plain message
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(char[] masterKey, String cipherContent) {
        try {
            byte[] decode = Base64.getDecoder().decode(cipherContent.getBytes(UTF_8));
            ByteBuffer byteBuffer = ByteBuffer.wrap(decode);

            byte[] salt = new byte[config.AES_SALT_LENGTH];
            byteBuffer.get(salt);

            byte[] iv = new byte[config.AES_NONCE_LENGTH];
            byteBuffer.get(iv);

            byte[] content = new byte[byteBuffer.remaining()];
            byteBuffer.get(content);

            SecretKey aesKeyFromPassword = getAESKeyFromPassword(masterKey, salt);

            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, aesKeyFromPassword, iv);
            assert cipher != null;
            byte[] plainText = cipher.doFinal(content);
            return new String(plainText, UTF_8);
        } catch (AEADBadTagException e) {
            LOGGER.log(SEVERE, "Decryption failed due to wrong masterKey:");
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.log(SEVERE, "Decryption failed due:");
        }
        return null;
    }

    /**
     * Generates a random nonce of the specified length.
     *
     * @param length the length of the nonce
     * @return the generated nonce
     */
    public static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        secureRandom.nextBytes(nonce);
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
            LOGGER.log(SEVERE, "Cipher initiation failed");
        } catch (InvalidKeyException e) {
            LOGGER.log(SEVERE, "Invalid key provided");
        }
        return null;
    }
}