package org.thegreycore.service;

import org.thegreycore.DTO.GetEntryDTO;
import org.thegreycore.DTO.NewEntryDTO;
import org.thegreycore.config.VaultConfig;

import java.sql.*;
import java.util.ArrayList;
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
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL)){
            if (connection == null) throw new SQLException("Connection is null");
            PreparedStatement stmt = connection.prepareStatement(
                    "insert into vault (encrypted_service, encrypted_username, encrypted_password) values (?, ?, ?)"
            );
            stmt.setString(1, cryptographyService.encrypt(entry.getMasterKey(), entry.getService()));
            stmt.setString(2, cryptographyService.encrypt(entry.getMasterKey(), entry.getUsername()));
            stmt.setString(3, cryptographyService.encrypt(entry.getMasterKey(), entry.getPassword()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred during adding entry: " ,e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public List<GetEntryDTO> getListOfEntries(String masterKey) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL)){
            if (connection == null) throw new SQLException("Connection is null");
            PreparedStatement stmt = connection.prepareStatement(
              "select id, encrypted_service, encrypted_username from vault"
            );
            ResultSet rs = stmt.executeQuery();
            List<GetEntryDTO> listOfEntries = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String encryptedService = rs.getString("encrypted_service");
                String encryptedUsername = rs.getString("encrypted_username");

                listOfEntries.add(new GetEntryDTO(id,
                        cryptographyService.decrypt(masterKey,encryptedService),
                        cryptographyService.decrypt(masterKey,encryptedUsername)));
            }
            return listOfEntries;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPassword(String masterKey, int id) {
        try (Connection connection = DriverManager.getConnection(config.VAULT_URL)){
            if (connection == null) throw new SQLException("Connection is null");
            PreparedStatement stmt = connection.prepareStatement(
                    "select encrypted_password from vault where id = ?"
            );
           stmt.setInt(1, id);
           ResultSet rs = stmt.executeQuery();
           if (rs.next()) {
               return cryptographyService.decrypt(masterKey, rs.getString("encrypted_password"));
           } else {
               return null;
           }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}