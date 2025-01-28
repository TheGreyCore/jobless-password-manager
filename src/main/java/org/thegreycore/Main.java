package org.thegreycore;

import org.thegreycore.dto.GetEntryDTO;
import org.thegreycore.dto.NewEntryDTO;
import org.thegreycore.config.VaultConfig;
import org.thegreycore.service.VaultService;

import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    public static final VaultService vault = new VaultService();
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String LOGO = """
                 ______  ___  __   ____________   \s
             __ / / __ \\/ _ \\/ /  / __/ __/ __/   \s
            / // / /_/ / ___/ /__/ _/_\\ \\_\\ \\     \s
            \\___/\\____/_/  /____/___/___/___/_____\s
              /  |/  / _ | / |/ / _ |/ ___/ __/ _ \\
             / /|_/ / __ |/    / __ / (_ / _// , _/
            /_/  /_/_/ |_/_/|_/_/ |_\\___/___/_/|_|\s
                                                  \s
            """;

    private static final String MENU = """
            
            +----------+----------------------------------+
            | Function |           Description            |
            +----------+----------------------------------+
            | (a)dd    | Add new entry to database        |
            | (g)et    | Get specific entry from database |
            | (r)emove | Remove entry from database       |
            | (e)xit   | Exit password manager            |
            +----------+----------------------------------+
            """;

    public static void main(String[] args) {
        System.out.println(LOGO);
        File f = new File("./vault.db");
        while (!f.exists()) {
            System.out.println("No vault found");
            VaultConfig vaultConfig = new VaultConfig();
            vaultConfig.createDatabase();
            f = new File("./vault.db");
        }

        System.out.printf("Found vault file: %s %n", f.getAbsolutePath());
        System.out.print("Please provide a master key: ");
        Scanner scanner = new Scanner(System.in);
        char[] masterKey = scanner.nextLine().toCharArray();
        List<GetEntryDTO> listOfEntries = vault.getListOfEntries(masterKey);
        listOfEntries.forEach(System.out::println);

        boolean exit = false;
        while (!exit) {
            System.out.println(MENU);
            String command = scanner.nextLine();
            switch (command.toLowerCase()) {
                case "a", "add":
                    addEntry();
                    break;
                case "g", "get":
                    getPassword();
                    break;
                case "r", "remove":
                    removeEntry();
                    break;
                case "e", "exit":
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid command");
                    break;
            }
        }
    }

    private static void removeEntry() {
        throw new UnsupportedOperationException();
    }

    private static void addEntry() {
        throw new UnsupportedOperationException();
    }

    private static void getPassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter ID to get the password:");
        int id = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter master key:");
        char[] masterKey = scanner.nextLine().toCharArray();
        System.out.println(vault.getPassword(masterKey, id));
    }

    public static void insertTestData() {
        vault.addEntry(new NewEntryDTO("Youtube", "test", "123123", "masterKey".toCharArray()));
    }
}