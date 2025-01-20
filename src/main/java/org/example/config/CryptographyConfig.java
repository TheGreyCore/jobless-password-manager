package org.example.config;


public class CryptographyConfig {

    public final int AES_KEY_SIZE = 256;
    public final int AES_IV_SIZE = 32;
    public final int AES_TAG_LENGTH_BITS = 128;
    public final int PBKDF2_ITERATIONS = 10_000;
    public final int PBKDF2_KEY_LENGTH = 256;
}
