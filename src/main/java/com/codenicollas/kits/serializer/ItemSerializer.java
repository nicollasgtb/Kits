package com.codenicollas.kits.serializer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemSerializer {

    private static final Logger LOGGER = Bukkit.getLogger();

    public static String serialize(ItemStack[] items) {
        if (items == null || items.length == 0) {
            LOGGER.fine("Attempting to serialize an array of null or empty items.");
            return "";
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(
                        byteArrayOutputStream)) {

            bukkitObjectOutputStream.writeInt(items.length);

            for (ItemStack item : items) {
                bukkitObjectOutputStream.writeObject(item != null ? item : new ItemStack(Material.AIR));
            }

            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());

        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Error serializing items.", exception);
            return "";
        }
    }

    public static ItemStack[] deserialize(String data) {
        if (data == null || data.isEmpty()) {
            LOGGER.warning("Attempt to deserialize a null or empty string.");
            return new ItemStack[0];
        }

        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
                BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream)) {

            int size = bukkitObjectInputStream.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) bukkitObjectInputStream.readObject();
            }

            return items;

        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            LOGGER.log(Level.SEVERE, "Error deserializing items", exception);
            return new ItemStack[0];
        }
    }
}