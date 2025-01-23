package org.thegreycore;

import org.thegreycore.service.CryptographyService;

public class Main {

    public static void main(String[] args){
        String encrypted = CryptographyService.encrypt("This_is_password", "This is not encrypt text");
        System.out.println(encrypted);
        String decrypted = CryptographyService.decrypt(encrypted, "Thisis_password");
        System.out.println(decrypted);
    }
}