package com.codenicollas.kits.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import com.codenicollas.kits.gui.KitEditorHolder;
import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.storage.repositories.KitRepository;

public class EditKitCommand implements CommandExecutor {

    private final KitRepository kitRepository;

    public EditKitCommand(KitRepository kitRepository) {
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
            player.sendMessage(ChatColor.RED + "Uso correto: /editarkit <id>");
            return false;
        }

        if (!player.hasPermission("command.editkit")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para executar este comando.");
            return false;
        }

        String id = args[0].toLowerCase();

        Kit kit = kitRepository.getKit(id);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "O kit " + id + " não existe.");
            return false;
        }

        KitEditorHolder holder = new KitEditorHolder(kit);

        Inventory kitInventory = Bukkit.createInventory(holder, 4 * 9, ChatColor.DARK_GRAY + "Kit");

        if (kit.getItems() != null) {
            kitInventory.setContents(kit.getItems());
        }

        player.openInventory(kitInventory);

        player.sendMessage(ChatColor.YELLOW + "Editando o kit " + id + ". Feche o inventário para salvar.");

        return true;
    }
}