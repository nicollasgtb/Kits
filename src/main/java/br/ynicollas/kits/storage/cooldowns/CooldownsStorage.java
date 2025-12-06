package br.ynicollas.kits.storage.cooldowns;

import br.ynicollas.kits.cache.CooldownsCache;
import br.ynicollas.kits.models.KitCooldown;
import br.ynicollas.kits.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CooldownsStorage {

    private final Database database;
    private final CooldownsCache cooldownsCache;
    private static final Logger LOGGER = Bukkit.getLogger();

    public CooldownsStorage(Database database, CooldownsCache cooldownsCache) {
        this.database = database;
        this.cooldownsCache = cooldownsCache;
    }

    public void addCooldown(Player player, String kit, KitCooldown cooldown) {
        long expireTime = System.currentTimeMillis() + cooldown.getMilliseconds();

        cooldownsCache.addCooldown(player, kit, expireTime);

        String query;
        if (database.getStorageType().equals("mariadb")) {
            query = "REPLACE INTO cooldowns (player, kit, expire_time) VALUES (?, ?, ?)";
        } else {
            query = "INSERT OR REPLACE INTO cooldowns (player, kit, expire_time) VALUES (?, ?, ?)";
        }

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, player.getName());
            statement.setString(2, kit);
            statement.setLong(3, expireTime);
            statement.executeUpdate();

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to set cooldown for player: " + player.getName(), exception);
        }
    }

    public long getCooldown(Player player, String id) {
        if (cooldownsCache.hasCooldown(player, id)) {
            return cooldownsCache.getCooldown(player, id);
        }

        String query = "SELECT expire_time FROM cooldowns WHERE player = ? AND kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, player.getName());
            statement.setString(2, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    long expireTime = resultSet.getLong("expire_time");

                    cooldownsCache.addCooldown(player, id, expireTime);

                    return expireTime;
                }
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to get cooldown for player: " + player.getName(), exception);
        }

        return 0;
    }

    public boolean hasCooldown(Player player, String kit) {
        long cooldownExpireTime = getCooldown(player, kit);

        return cooldownExpireTime > System.currentTimeMillis();
    }

    public void removeCooldown(Player player, String kit) {
        String query = "DELETE FROM cooldowns WHERE player = ? AND kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, player.getName());
            statement.setString(2, kit);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                cooldownsCache.removeCooldown(player, kit);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to remove cooldown for player", exception);
        }
    }

    public void clear(String kit) {
        String query = "DELETE FROM cooldowns WHERE kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, kit);
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                cooldownsCache.removeCooldownsForKit(kit);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to clear cooldowns for kit: " + kit, exception);
        }
    }
}