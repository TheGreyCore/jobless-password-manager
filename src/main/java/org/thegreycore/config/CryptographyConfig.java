package org.thegreycore.config;


public class CryptographyConfig {

    public final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA512";
    public final int AES_NONCE_LENGTH = 12;
    public final int AES_TAG_LENGTH_BITS = 128;
    public final int PBKDF2_ITERATIONS = 100_000;
    public final int PBKDF2_KEY_LENGTH = 256;
    public final int AES_SALT_LENGTH = 32;
}
