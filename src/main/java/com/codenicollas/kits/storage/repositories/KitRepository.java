package com.codenicollas.kits.storage.repositories;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import com.codenicollas.kits.cache.KitCache;
import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.model.KitCooldown;
import com.codenicollas.kits.serializer.ItemSerializer;
import com.codenicollas.kits.storage.Database;
import com.codenicollas.kits.util.TimeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KitRepository {

    private final Database database;
    private final KitCache kitCache;

    private static final Logger LOGGER = Bukkit.getLogger();

    public KitRepository(Database database, KitCache kitCache) {
        this.kitCache = kitCache;
        this.database = database;
    }

    public void saveKit(Kit kit) {
        String query;

        if (database.getStorageType().equals("mariadb")) {
            query = "REPLACE INTO kits (kit, permission, cooldown, content) VALUES (?, ?, ?, ?)";
        } else {
            query = "INSERT OR REPLACE INTO kits (kit, permission, cooldown, content) VALUES (?, ?, ?, ?)";
        }

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, kit.getId());
            statement.setString(2, kit.getPermission());
            statement.setLong(3, kit.getCooldown().getMilliseconds());
            statement.setString(4, ItemSerializer.serialize(kit.getItems()));

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                kitCache.addKit(kit);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to save kit: " + kit.getId(), exception);
        }
    }

    public Kit getKit(String id) {
        if (kitCache.containsKit(id)) {
            return kitCache.getKit(id);
        }

        String query = "SELECT * FROM kits WHERE kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String permission = resultSet.getString("permission");
                    long cooldownInMillis = resultSet.getLong("cooldown");
                    String serializedItems = resultSet.getString("content");

                    KitCooldown cooldown = TimeUtils.convertToCooldown(cooldownInMillis);
                    ItemStack[] items = ItemSerializer.deserialize(serializedItems);

                    Kit kit = new Kit(id, permission, cooldown, items);

                    kitCache.addKit(kit);

                    return kit;
                }
            }
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve kit: " + id, exception);
        }

        return null;
    }

    public void removeKit(String id) {
        String query = "DELETE FROM kits WHERE kit = ?";

        try (Connection connection = database.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, id);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                kitCache.removeKit(id);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to remove kit: " + id, exception);
        }
    }
}