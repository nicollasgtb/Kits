package br.ynicollas.kits.listeners;

import br.ynicollas.kits.gui.KitEditorHolder;
import br.ynicollas.kits.models.Kit;
import br.ynicollas.kits.storage.kits.KitsStorage;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryCloseListener implements Listener {

    private final KitsStorage kits;

    public InventoryCloseListener(KitsStorage kits) {
        this.kits = kits;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (!(inventory.getHolder() instanceof KitEditorHolder)) {
            return;
        }

        KitEditorHolder holder = (KitEditorHolder) inventory.getHolder();
        Kit kit = holder.getKit();

        List<ItemStack> filteredItems = new ArrayList<>();

        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                filteredItems.add(item);
            }
        }

        kit.setItems(filteredItems.toArray(new ItemStack[0]));

        kits.saveKit(kit);
    }
}