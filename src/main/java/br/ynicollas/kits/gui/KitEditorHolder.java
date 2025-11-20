package br.ynicollas.kits.gui;

import br.ynicollas.kits.models.Kit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

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