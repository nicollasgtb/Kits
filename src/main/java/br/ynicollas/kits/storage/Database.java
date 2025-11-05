package br.ynicollas.kits.storage;

import br.ynicollas.kits.KitsPlugin;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private Connection connection;

    private final File dataFolder;
    private final Logger logger;

    public Database(KitsPlugin plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.logger = plugin.getLogger();
    }

    private String getDatabaseUrl() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.log(Level.SEVERE, "Failed to create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        File databaseFile = new File(dataFolder, "kits.db");
        return "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    public void openConnection() {
        synchronized (this) {
            try {
                if (connection == null || connection.isClosed()) {
                    Class.forName("org.sqlite.JDBC");

                    connection = DriverManager.getConnection(getDatabaseUrl());

                    createTables();
                }
            } catch (ClassNotFoundException | SQLException exception) {
                logger.log(Level.SEVERE, "Failed to open database connection.", exception);
            }
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                openConnection();
            }

        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Error checking database connection.", exception);
        }

        return connection;
    }

    public void closeConnection() {
        synchronized (this) {
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    connection = null;
                }

            } catch (SQLException exception) {
                logger.log(Level.SEVERE, "Failed to close database connection.", exception);
            }
        }
    }

    private void createTables() {
        if (connection == null) {
            logger.log(Level.SEVERE, "Cannot create tables: No database connection.");
            return;
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS kits (kit TEXT PRIMARY KEY, permission TEXT, cooldown INTEGER, content TEXT)");
            statement.execute("CREATE TABLE IF NOT EXISTS cooldowns (player TEXT, kit TEXT, expire_time INTEGER, PRIMARY KEY(player, kit))");
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Failed to create database tables.", exception);
        }
    }
}