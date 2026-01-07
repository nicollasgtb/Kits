package com.codenicollas.kits.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.codenicollas.kits.gui.KitPreviewHolder;
import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.storage.repositories.KitRepository;

public class ViewKitCommand implements CommandExecutor {

    private final KitRepository kitRepository;

    public ViewKitCommand(KitRepository kitRepository) {
        this.kitRepository = kitRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem utilizar este comando.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso correto: /verkit <id>");
            return false;
        }

        String id = args[0].toLowerCase();

        Kit kit = kitRepository.getKit(id);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "O kit '" + id + "' não existe.");
            return false;
        }

        KitPreviewHolder holder = new KitPreviewHolder();

        Inventory kitInventory = Bukkit.createInventory(holder, 54,
                ChatColor.DARK_GRAY + "Visualizando Kit: " + kit.getId());

        if (kit.getItems() != null) {
            kitInventory.setContents(kit.getItems());
        }

        player.openInventory(kitInventory);

        return true;
    }
}