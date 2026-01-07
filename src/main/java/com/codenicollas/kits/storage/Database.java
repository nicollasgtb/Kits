package com.codenicollas.kits.storage;

import com.codenicollas.kits.KitsPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {

    private final Logger logger;
    private final File dataFolder;
    private final FileConfiguration config;

    private HikariDataSource dataSource;
    private String storageType;

    public Database(KitsPlugin plugin) {
        this.logger = plugin.getLogger();
        this.dataFolder = plugin.getDataFolder();
        this.config = plugin.getConfig();
    }

    public void openConnection() {
        this.storageType = config.getString("storage.type", "sqlite").toLowerCase();

        HikariConfig hikariConfig = new HikariConfig();

        try {
            if (storageType.equals("mariadb")) {
                logger.info("Storage type set to MariaDB. Connecting...");

                hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");

                hikariConfig.setJdbcUrl("jdbc:mariadb://" +
                        config.getString("storage.mariadb.host", "127.0.0.1") + ":" +
                        config.getInt("storage.mariadb.port", 3306) + "/" +
                        config.getString("storage.mariadb.database", "kits"));

                hikariConfig.setUsername(config.getString("storage.mariadb.username", "root"));
                hikariConfig.setPassword(config.getString("storage.mariadb.password", "password"));

            } else {
                logger.info("Storage type set to SQLite. Connecting...");

                hikariConfig.setMaximumPoolSize(1);
                hikariConfig.setDriverClassName("org.sqlite.JDBC");
                hikariConfig.setJdbcUrl(getSQLiteUrl());

                hikariConfig.setConnectionTestQuery("SELECT 1");
            }

            hikariConfig.setPoolName("kits-pool" + storageType.toUpperCase());

            hikariConfig.setMaximumPoolSize(config.getInt("pool-settings.maximum-pool-size", 3));
            hikariConfig.setMinimumIdle(config.getInt("pool-settings.minimum-idle", 1));
            hikariConfig.setMaxLifetime(config.getLong("pool-settings.maximum-lifetime", 1800000));
            hikariConfig.setConnectionTimeout(config.getLong("pool-settings.connection-timeout", 10000));

            this.dataSource = new HikariDataSource(hikariConfig);

            createTables();
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Failed to open database connection.", exception);
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection is not initialized.");
        }

        return dataSource.getConnection();
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public String getStorageType() {
        if (this.storageType == null) {
            return config.getString("storage.type", "sqlite").toLowerCase();
        }

        return this.storageType;
    }

    private String getSQLiteUrl() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            logger.log(Level.SEVERE, "Failed to create plugin data folder: " + dataFolder.getAbsolutePath());
        }

        File databaseFile = new File(dataFolder, "kits.db");
        
        return "jdbc:sqlite:" + databaseFile.getAbsolutePath();
    }

    private void createTables() {
        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {

            String kitsTable = "CREATE TABLE IF NOT EXISTS kits (" +
                    "kit VARCHAR(64) NOT NULL PRIMARY KEY, " +
                    "permission VARCHAR(64), " +
                    "cooldown BIGINT DEFAULT 0, " +
                    "content TEXT NOT NULL" +
                    ")";

            String cooldownsTable = "CREATE TABLE IF NOT EXISTS cooldowns (" +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "kit VARCHAR(64) NOT NULL, " +
                    "expire_time BIGINT NOT NULL, " +
                    "PRIMARY KEY(player, kit)" +
                    ")";

            statement.execute(kitsTable);
            statement.execute(cooldownsTable);

        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Failed to create database tables.", exception);
        }
    }
}