package org.thegreycore;

import org.thegreycore.DTO.GetEntryDTO;
import org.thegreycore.DTO.NewEntryDTO;
import org.thegreycore.config.VaultConfig;
import org.thegreycore.service.VaultService;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final VaultService vault = new VaultService();

    public static void main(String[] args){
        while(true){
            File f = new File("./vault.db");
            if(f.exists()){
                System.out.printf("Found vault file: %s\n", f.getAbsolutePath());
                System.out.println("Please provide a master key!");
                Scanner scanner = new Scanner(System.in);
                String masterKey = scanner.nextLine();
                List<GetEntryDTO> listOfEntries = vault.getListOfEntries(masterKey);
                masterKey = " ";
                listOfEntries.forEach(System.out::println);
                System.out.println("Enter ID to get the password:");
                int id = Integer.parseInt(scanner.nextLine());
                System.out.println("Enter master key:");
                masterKey = scanner.nextLine();
                System.out.println(vault.getPassword(masterKey, id));
                masterKey = " ";
                break;
            } else{
                System.out.println("No vault found");
                VaultConfig vaultConfig = new VaultConfig();
                vaultConfig.createDatabase();
            }
        }
    }

    public static void insertTestData(){
        vault.addEntry(new NewEntryDTO("Youtube", "test", "123123", "masterKey"));
    }
}