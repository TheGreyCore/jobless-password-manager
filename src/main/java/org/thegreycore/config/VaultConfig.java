package org.thegreycore.config;

import org.thegreycore.service.CryptographyService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VaultConfig {
    private static final Logger LOGGER = Logger.getLogger(VaultConfig.class.getName());
    public final String VAULT_URL = "jdbc:sqlite:vault.db";

    /**
     * Method for creating SQLite3 database.
     */
    public void createDatabase() {
        try (Connection conn = DriverManager.getConnection(VAULT_URL)) {
            if (conn != null) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS vault ("
                        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + "encrypted_service String NOT NULL,"
                        + "encrypted_username String NOT NULL,"
                        + "encrypted_password String NOT NULL"
                        + ");";
                PreparedStatement stmt = conn.prepareStatement(createTableSQL);
                stmt.executeUpdate();
                LOGGER.log(Level.INFO, "Vault table and database created");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQLException occurred when creating database!", e);
            throw new RuntimeException(e);
        }
    }
}
