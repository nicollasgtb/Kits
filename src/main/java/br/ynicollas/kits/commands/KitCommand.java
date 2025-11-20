package br.ynicollas.kits.commands;

import br.ynicollas.kits.models.Kit;
import br.ynicollas.kits.models.KitCooldown;
import br.ynicollas.kits.storage.cooldowns.CooldownsStorage;
import br.ynicollas.kits.storage.kits.KitsStorage;
import br.ynicollas.kits.util.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitCommand implements CommandExecutor {

    private final CooldownsStorage cooldowns;
    private final KitsStorage kits;

    public KitCommand(CooldownsStorage cooldowns, KitsStorage kits) {
        this.cooldowns = cooldowns;
        this.kits = kits;
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

        Kit kit = kits.getKit(id);

        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit não encontrado!");
            return false;
        }

        if (!player.hasPermission(kit.getPermission())) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para usar este kit!");
            return false;
        }

        if (cooldowns.hasCooldown(player, id) && !player.hasPermission("kit.bypass")) {
            long timeRemaining = cooldowns.getCooldown(player, id) - System.currentTimeMillis();
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

        cooldowns.removeCooldown(player, id);
        cooldowns.addCooldown(player, id, cooldown);

        player.sendMessage(ChatColor.YELLOW + "Você coletou o kit com sucesso!");

        return true;
    }
}