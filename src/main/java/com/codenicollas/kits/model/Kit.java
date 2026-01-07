package com.codenicollas.kits.model;

import org.bukkit.inventory.ItemStack;

public class Kit {

    private final String id;
    private final String permission;
    private final KitCooldown cooldown;
    private ItemStack[] items;

    public Kit(String id, String permission, KitCooldown cooldown, ItemStack[] items) {
        this.id = id;
        this.permission = permission;
        this.cooldown = cooldown;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public KitCooldown getCooldown() {
        return cooldown;
    }

    public ItemStack[] getItems() {
        return items;
    }

    public void setItems(ItemStack[] items) {
        this.items = items;
    }
}