package org.example;

import org.example.service.CryptographyService;

import javax.crypto.SecretKey;

public class Main {
    private final static CryptographyService cryptographyService = new CryptographyService();
    public static void main(String[] args) {
        SecretKey secretKey = cryptographyService.getAESKeyFromPassword("this_is_the_password");
        System.out.println(secretKey);
    }
}