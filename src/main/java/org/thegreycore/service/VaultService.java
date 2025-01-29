package org.thegreycore.service;

import org.thegreycore.dto.GetEntryDTO;
import org.thegreycore.dto.NewEntryDTO;
import org.thegreycore.config.VaultConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaultService {
    private static final Logger LOGGER = Logger.getLogger(VaultService.class.getName());
    private static final VaultConfig config = new VaultConfig();
    private static final CryptographyService cryptographyService = new CryptographyService();

    /**
     * Adds new entry to vault.
     *
     * @param entry to be added.
     * @throws RuntimeException if error occurs during adding entry.
     */
    public void addEntry(NewEntryDTO entry) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL)) {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "insert into vault (encrypted_service, encrypted_username, encrypted_password) values (?, ?, ?)"
            )) {
                stmt.setString(1, cryptographyService.encrypt(entry.getMasterKey(), entry.getService()));
                stmt.setString(2, cryptographyService.encrypt(entry.getMasterKey(), entry.getUsername()));
                stmt.setString(3, cryptographyService.encrypt(entry.getMasterKey(), entry.getPassword()));
                stmt.executeUpdate();
            } finally {
                entry.setMasterKey(null);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during adding entry to database");
        }
    }

    /**
     * Retrieves a list of vault entries from the database, decrypting sensitive fields using the provided master key.
     * The master key is securely cleared (zeroed) after processing. If an error occurs during database access,
     * an empty list is returned and an error is logged.
     *
     * @param masterKey The encryption key used to decrypt sensitive fields (service name and username).
     *                  This array is zeroed out after processing to prevent memory exposure.
     * @return A {@link List} of {@link GetEntryDTO} objects containing decrypted entry data.
     * Returns an empty list if no entries exist or if a database error occurs.
     * @see GetEntryDTO
     * @see CryptographyService#decrypt(char[], String)
     */
    public List<GetEntryDTO> getListOfEntries(char[] masterKey) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL);
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT id, encrypted_service, encrypted_username FROM vault"
             )) {

            ResultSet rs = stmt.executeQuery();
            List<GetEntryDTO> listOfEntries = new ArrayList<>();

            while (rs.next()) {
                int id = rs.getInt("id");
                String encryptedService = rs.getString("encrypted_service");
                String encryptedUsername = rs.getString("encrypted_username");

                String decryptedService = cryptographyService.decrypt(masterKey, encryptedService);
                if (decryptedService == null) continue;

                listOfEntries.add(new GetEntryDTO(
                        id,
                        decryptedService,
                        cryptographyService.decrypt(masterKey, encryptedUsername)
                ));
            }

            return listOfEntries;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during database access", e);
        } finally {
            java.util.Arrays.fill(masterKey, '\0');
        }

        return Collections.emptyList();
    }

    /**
     * Retrieves and decrypts the password for a specific vault entry identified by its ID using the provided master key.
     * If a database error occurs or the entry is not found, returns {@code null} and logs the error.
     *
     * @param masterKey The encryption key used to decrypt the stored password.
     * @param id        The unique identifier of the vault entry to retrieve.
     * @return The decrypted password as a {@link String}, or {@code null} if the entry does not exist
     * or a database access error occurs.
     * @see CryptographyService#decrypt(char[], String)
     */
    public String getPassword(char[] masterKey, int id) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL);
             PreparedStatement stmt = connection.prepareStatement(
                     "select password from vault where id = ?"
             )) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return cryptographyService.decrypt(masterKey, rs.getString("encrypted_password"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during getting password from database", e);
        }
        return null;
    }


    /**
     * Deletes an entry from the vault table based on the specified ID.
     *
     * @param id The unique identifier of the entry to be deleted. Must be a positive integer
     *           corresponding to an existing entry.
     */
    public void deleteEntry(int id) {
        if (id <= 0) {
            String errorMsg = String.format("Invalid ID '%d' - must be a positive integer", id);
            LOGGER.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try (Connection connection = DriverManager.getConnection(config.VAULT_URL);
             PreparedStatement stmt = connection.prepareStatement(
                     "DELETE FROM vault WHERE id = ?"
             )) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "No entry found with ID {0} - deletion skipped", id);
            }
        } catch (SQLException e) {
            String errorMsg = String.format("Error deleting entry with ID %d from database", id);
            LOGGER.log(Level.SEVERE, errorMsg, e);
        }
    }
}