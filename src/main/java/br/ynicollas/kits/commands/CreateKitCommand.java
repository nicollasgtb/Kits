package br.ynicollas.kits.commands;

import br.ynicollas.kits.gui.KitEditorHolder;
import br.ynicollas.kits.models.Kit;
import br.ynicollas.kits.models.KitCooldown;
import br.ynicollas.kits.storage.kits.KitsStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class CreateKitCommand implements CommandExecutor {

    private final KitsStorage kits;

    public CreateKitCommand(KitsStorage kits) {
        this.kits = kits;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Apenas jogadores podem utilizar este comando.");
            return false;
        }

        Player player = (Player) sender;

        if (args.length != 3) {
            player.sendMessage(ChatColor.RED + "Uso correto: /criarkit <nome> <permissão> <tempo>");
            return false;
        }

        if (!player.hasPermission("command.createkit")) {
            player.sendMessage(ChatColor.RED + "Você não tem permissão para executar este comando.");
            return false;
        }

        String id = args[0].toLowerCase();
        String permission = args[1];
        String cooldownStr = args[2];

        Kit existingKit = kits.getKit(id);

        if (existingKit != null) {
            player.sendMessage(ChatColor.RED + "Já existe um kit com esse nome.");
            return false;
        }

        int cooldown;

        try {
            cooldown = Integer.parseInt(cooldownStr);
        } catch (NumberFormatException exception) {
            player.sendMessage(ChatColor.RED + "O cooldown deve ser um número válido.");
            return false;
        }

        KitCooldown kitCooldown = new KitCooldown(0, 0, cooldown);
        Kit kit = new Kit(id, permission, kitCooldown, null);

        KitEditorHolder holder = new KitEditorHolder(kit);

        Inventory kitInventory = Bukkit.createInventory(holder, 54, ChatColor.DARK_GRAY + "Kit");
        player.openInventory(kitInventory);

        player.sendMessage(ChatColor.YELLOW + "Kit criado! Coloque os itens no inventário e feche para salvar.");

        return true;
    }
}