package org.thegreycore.config;


public class CryptographyConfig {

    public final int ARGON2_ITERATIONS = 4;
    public final int ARGON2_MEMOMORY_LIMIT = 131072; // 128MB
    public final int ARGON2_AES_KEY_LENGTH = 32;
    public final int ARGON2_PARALLELISM = 4;
    public final int AES_NONCE_LENGTH = 12;
    public final int AES_TAG_LENGTH_BITS = 128;
    public final int AES_SALT_LENGTH = 32;
    public final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
}
