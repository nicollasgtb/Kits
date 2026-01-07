package com.codenicollas.kits.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.codenicollas.kits.model.Kit;

public class KitEditorHolder implements InventoryHolder {

    private final Kit kit;

    public KitEditorHolder(Kit kit) {
        this.kit = kit;
    }

    public Kit getKit() {
        return kit;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}