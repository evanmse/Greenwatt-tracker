package fr.greenwatt.repository;

import fr.greenwatt.exception.StockageException;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pattern Singleton via ENUM.
 * Gère une connexion JDBC SQLite unique.
 */
public enum GestionnaireSQLite {

    INSTANCE;

    private final Connection connection;

    GestionnaireSQLite() {
        try {
            new File("data").mkdirs();
            this.connection = DriverManager.getConnection("jdbc:sqlite:data/greenwatt.db");
            initialiserSchema();
        } catch (SQLException e) {
            throw new StockageException("Connexion SQLite impossible", e);
        }
    }

    public Connection getConnection() { return connection; }

    private void initialiserSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS batiment(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    denomination TEXT NOT NULL,
                    ville TEXT, region TEXT, code_postal TEXT, zone TEXT,
                    surface REAL NOT NULL,
                    occupants INTEGER NOT NULL,
                    source TEXT
                )""");
            st.execute("""
                CREATE TABLE IF NOT EXISTS mesure(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    batiment_id INTEGER NOT NULL,
                    categorie TEXT NOT NULL,
                    quantite REAL NOT NULL,
                    unite TEXT,
                    horodatage TEXT,
                    FOREIGN KEY(batiment_id) REFERENCES batiment(id) ON DELETE CASCADE
                )""");
        }
    }
}
