package br.ynicollas.kits.commands;

import br.ynicollas.kits.gui.KitPreviewHolder;
import br.ynicollas.kits.models.Kit;
import br.ynicollas.kits.storage.kits.KitsStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ViewKitCommand implements CommandExecutor {

    private final KitsStorage kits;

    public ViewKitCommand(KitsStorage kits) {
        this.kits = kits;
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

        Kit kit = kits.getKit(id);

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