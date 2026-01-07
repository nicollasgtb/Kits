package com.codenicollas.kits.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.storage.repositories.KitRepository;

public class GiveKitCommand implements CommandExecutor {

    private final KitRepository kitRepository;

    public GiveKitCommand(KitRepository kitRepository) {
        this.kitRepository = kitRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /givekit <player> <id>");
            return false;
        }

        if (!sender.hasPermission("command.givekit")) {
            sender.sendMessage(ChatColor.RED + "Você não possui permissão para executar este comando.");
            return false;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Jogador não encontrado.");
            return false;
        }

        String id = args[1].toLowerCase();

        Kit kit = kitRepository.getKit(id);

        if (kit == null) {
            sender.sendMessage(ChatColor.RED + "O kit " + id + " não existe.");
            return false;
        }

        for (ItemStack item : kit.getItems()) {
            if (item != null) {
                target.getInventory().addItem(item);
            }
        }

        sender.sendMessage(ChatColor.YELLOW + "Kit enviado com sucesso!");

        return true;
    }
}