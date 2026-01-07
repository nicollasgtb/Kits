package com.codenicollas.kits.storage.repositories;

import com.codenicollas.kits.cache.CooldownCache;
import com.codenicollas.kits.model.KitCooldown;
import com.codenicollas.kits.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CooldownRepository {

    private final Database database;
    private final CooldownCache cooldownCache;
    
    private static final Logger LOGGER = Bukkit.getLogger();

    public CooldownRepository(Database database, CooldownCache cooldownCache) {
        this.database = database;
        this.cooldownCache = cooldownCache;
    }

    public void addCooldown(Player player, String kit, KitCooldown cooldown) {
        long expireTime = System.currentTimeMillis() + cooldown.getMilliseconds();
        String uuid = player.getUniqueId().toString();

        cooldownCache.addCooldown(player, kit, expireTime);

        CompletableFuture.runAsync(() -> {
            String query;
            if (database.getStorageType().equals("mariadb")) {
                query = "INSERT INTO cooldowns (player_uuid, kit, expire_time) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE expire_time = VALUES(expire_time)";
            } else {
                query = "INSERT OR REPLACE INTO cooldowns (player_uuid, kit, expire_time) VALUES (?, ?, ?)";
            }

            try (Connection connection = database.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, uuid);
                statement.setString(2, kit);
                statement.setLong(3, expireTime);
                statement.executeUpdate();

            } catch (SQLException exception) {
                LOGGER.log(Level.SEVERE, "Failed to set cooldown for player: " + player.getName(), exception);
            }
        });
    }

    public long getCooldown(Player player, String kit) {
        if (cooldownCache.hasCooldown(player, kit)) {
            return cooldownCache.getCooldown(player, kit);
        }

        String uuid = player.getUniqueId().toString();
        String query = "SELECT expire_time FROM cooldowns WHERE player_uuid = ? AND kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, uuid);
            statement.setString(2, kit);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long expireTime = resultSet.getLong("expire_time");

                    cooldownCache.addCooldown(player, kit, expireTime);

                    return expireTime;
                }
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to get cooldown for player: " + player.getName(), exception);
        }

        return 0;
    }

    public void loadCooldowns(Player player) {
        String uuid = player.getUniqueId().toString();
        long now = System.currentTimeMillis();

        String query = "SELECT kit, expire_time FROM cooldowns WHERE player_uuid = ? AND expire_time > ?";

        CompletableFuture.runAsync(() -> {
            try (Connection connection = database.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, uuid);
                statement.setLong(2, now);

                try (ResultSet resultSet = statement.executeQuery()) {
                    Map<String, Long> loadedData = new HashMap<>();

                    while (resultSet.next()) {
                        String kitName = resultSet.getString("kit");
                        long expireTime = resultSet.getLong("expire_time");
                        loadedData.put(kitName, expireTime);
                    }

                    if (!loadedData.isEmpty()) {
                        cooldownCache.loadCooldowns(player, loadedData);
                    }
                }

            } catch (SQLException exception) {
                LOGGER.log(Level.SEVERE, "Failed to load cooldowns for player: " + player.getName(), exception);
            }
        });
    }

    public boolean hasCooldown(Player player, String kit) {
        long cooldownExpireTime = getCooldown(player, kit);
        return cooldownExpireTime > System.currentTimeMillis();
    }

    public void removeCooldown(Player player, String kit) {
        cooldownCache.removeCooldown(player, kit);

        String uuid = player.getUniqueId().toString();

        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM cooldowns WHERE player_uuid = ? AND kit = ?";

            try (Connection connection = database.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, uuid);
                statement.setString(2, kit);
                statement.executeUpdate();
            } catch (SQLException exception) {
                LOGGER.log(Level.SEVERE, "Failed to remove cooldown for player", exception);
            }
        });
    }

    public void clear(String kit) {
        cooldownCache.removeCooldownsForKit(kit);

        CompletableFuture.runAsync(() -> {
            String query = "DELETE FROM cooldowns WHERE kit = ?";

            try (Connection connection = database.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, kit);
                statement.executeUpdate();
            } catch (SQLException exception) {
                LOGGER.log(Level.SEVERE, "Failed to clear cooldowns for kit: " + kit, exception);
            }
        });
    }
}