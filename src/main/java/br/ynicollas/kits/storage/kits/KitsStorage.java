package br.ynicollas.kits.storage.kits;

import br.ynicollas.kits.cache.KitsCache;
import br.ynicollas.kits.models.Kit;
import br.ynicollas.kits.models.KitCooldown;
import br.ynicollas.kits.storage.Database;
import br.ynicollas.kits.serializer.ItemSerializer;
import br.ynicollas.kits.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KitsStorage {

    private final Database database;
    private final KitsCache kitsCache;

    private static final Logger LOGGER = Bukkit.getLogger();

    public KitsStorage(Database database, KitsCache kitsCache) {
        this.kitsCache = kitsCache;
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
                kitsCache.addKit(kit);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to save kit: " + kit.getId(), exception);
        }
    }

    public Kit getKit(String id) {
        if (kitsCache.containsKit(id)) {
            return kitsCache.getKit(id);
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

                    kitsCache.addKit(kit);

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
                kitsCache.removeKit(id);
            }

        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Failed to remove kit: " + id, exception);
        }
    }
}