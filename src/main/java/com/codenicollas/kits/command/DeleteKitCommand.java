package com.codenicollas.kits.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.storage.repositories.CooldownRepository;
import com.codenicollas.kits.storage.repositories.KitRepository;

public class DeleteKitCommand implements CommandExecutor {

    private final CooldownRepository cooldownRepository;
    private final KitRepository kitRepository;

    public DeleteKitCommand(CooldownRepository cooldownRepository, KitRepository kitRepository) {
        this.cooldownRepository = cooldownRepository;
        this.kitRepository = kitRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Uso correto: /deletekit <id>");
            return false;
        }

        if (!sender.hasPermission("command.deletekit")) {
            sender.sendMessage(ChatColor.RED + "Você não tem permissão para executar este comando.");
            return false;
        }

        String id = args[0].toLowerCase();

        Kit kit = kitRepository.getKit(id);

        if (kit == null) {
            sender.sendMessage(ChatColor.RED + "Kit não encontrado!");
            return false;
        }

        cooldownRepository.clear(id);
        kitRepository.removeKit(id);

        sender.sendMessage(ChatColor.YELLOW + "Kit deletado com sucesso!");

        return true;
    }
}