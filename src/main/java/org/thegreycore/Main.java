package org.thegreycore;

import org.thegreycore.dto.NewEntryDTO;
import org.thegreycore.config.VaultConfig;
import org.thegreycore.service.VaultService;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Logger;

public class Main {
    public static final VaultService vault = new VaultService();
    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String LOGO = """
                   ______  ____  __    ________________   \s
                  / / __ \\/ __ )/ /   / ____/ ___/ ___/   \s
             __  / / / / / __  / /   / __/  \\__ \\\\__ \\    \s
            / /_/ / /_/ / /_/ / /___/ /___ ___/ /__/ /    \s
            \\____/\\____/_____/_____/_____//____/____/_____\s
               /  |/  /   |  / | / /   | / ____/ ____/ __ \\
              / /|_/ / /| | /  |/ / /| |/ / __/ __/ / /_/ /
             / /  / / ___ |/ /|  / ___ / /_/ / /___/ _, _/\s
            /_/  /_/_/  |_/_/ |_/_/  |_\\____/_____/_/ |_| \s
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
        Scanner scanner = new Scanner(System.in); // Single Scanner instance
        char[] masterKey = scanner.nextLine().toCharArray();
        showListOfEntries(masterKey);

        boolean exit = false;
        while (!exit) {
            System.out.println(MENU);
            String command = scanner.nextLine(); // Read command
            switch (command.toLowerCase()) {
                case "a", "add":
                    addEntry(scanner); // Pass scanner to method
                    break;
                case "g", "get":
                    getPassword(scanner); // Pass scanner to method
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
        scanner.close(); // Close scanner only at the end
    }

    private static void removeEntry() {
        throw new UnsupportedOperationException();
    }

    private static void showListOfEntries(char[] masterKey) {
        vault.getListOfEntries(masterKey).forEach(System.out::println);
    }

    // Updated methods to reuse the main Scanner
    private static void addEntry(Scanner scanner) {
        System.out.println("Please provide a master key: ");
        char[] masterKey = scanner.nextLine().toCharArray();
        System.out.println("Please provide service name, username and password separated by a space");
        String[] data = scanner.nextLine().split(" ");
        NewEntryDTO entryDTO = new NewEntryDTO(data[0], data[1], data[2], masterKey);
        vault.addEntry(entryDTO);
        showListOfEntries(masterKey);
    }

    private static void getPassword(Scanner scanner) {
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