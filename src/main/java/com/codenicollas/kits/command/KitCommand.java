package com.codenicollas.kits.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.codenicollas.kits.model.Kit;
import com.codenicollas.kits.model.KitCooldown;
import com.codenicollas.kits.storage.repositories.CooldownRepository;
import com.codenicollas.kits.storage.repositories.KitRepository;
import com.codenicollas.kits.util.TimeUtils;

public class KitCommand implements CommandExecutor {

    private final CooldownRepository cooldownRepository;
    private final KitRepository kitRepository;

    public KitCommand(CooldownRepository cooldownRepository, KitRepository kitRepository) {
        this.cooldownRepository = cooldownRepository;
        this.kitRepository = kitRepository;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem utilizar este comando");
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Uso: /kit <id>");
            return false;
        }

        String id = args[0].toLowerCase();

        Kit kit = kitRepository.getKit(id);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit não encontrado!");
            return false;
        }

        if (!player.hasPermission(kit.getPermission())) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este kit!");
            return false;
        }

        if (cooldownRepository.hasCooldown(player, id) && !player.hasPermission("kit.bypass")) {
            long timeRemaining = cooldownRepository.getCooldown(player, id) - System.currentTimeMillis();
            player.sendMessage(ChatColor.RED + "Você precisa esperar " + TimeUtils.format(timeRemaining)
                    + " para pegar este kit novamente.");
            return false;
        }

        for (ItemStack item : kit.getItems()) {
            if (item != null) {
                player.getInventory().addItem(item);
            }
        }

        KitCooldown cooldown = kit.getCooldown();

        cooldownRepository.removeCooldown(player, id);
        cooldownRepository.addCooldown(player, id, cooldown);

        player.sendMessage(ChatColor.YELLOW + "Você coletou o kit com sucesso!");

        return true;
    }
}