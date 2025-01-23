package org.thegreycore.service;

import org.thegreycore.DTO.EntryDTO;
import org.thegreycore.config.VaultConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaultService {
    private static final Logger LOGGER = Logger.getLogger(VaultService.class.getName());
    private static final VaultConfig config = new VaultConfig();

    /**
     * Adds new entry to vault.
     *
     * @param entry to be added.
     * @throws RuntimeException if error occurs during adding entry.
     */
    public static void addEntry(EntryDTO entry) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL)){
            if (connection == null) throw new SQLException("Connection is null");
            PreparedStatement stmt = connection.prepareStatement(
                    "insert into vault_entry (encrypted_service, encrypted_username, encrypted_password) values (?, ?, ?)"
            );
            stmt.setString(1, entry.getEncryptedService());
            stmt.setString(2, entry.getEncryptedUsername());
            stmt.setString(3, entry.getEncryptedPassword());
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during adding entry: " ,e.getMessage());
            throw new RuntimeException(e);
        }
    }
}