package com.codenicollas.kits;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.codenicollas.kits.cache.CooldownCache;
import com.codenicollas.kits.cache.KitCache;
import com.codenicollas.kits.command.*;
import com.codenicollas.kits.listener.InventoryClickListener;
import com.codenicollas.kits.listener.InventoryCloseListener;
import com.codenicollas.kits.listener.PlayerSessionListener;
import com.codenicollas.kits.storage.Database;
import com.codenicollas.kits.storage.repositories.CooldownRepository;
import com.codenicollas.kits.storage.repositories.KitRepository;;

public class KitsPlugin extends JavaPlugin {

    private Database database;

    private CooldownRepository cooldownRepository;
    private KitRepository kitRepository;

    private CooldownCache cooldownsCache;

    @Override
    public void onEnable() {
        getLogger().info("Starting Kits v" + getDescription().getVersion() + "...");

        saveDefaultConfig();

        setupDatabase();

        registerCommands();
        registerListeners();

        getLogger().info("Plugin Kits v" + getDescription().getVersion() + " successfully enabled!");
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.closeConnection();
        }

        getLogger().info("Plugin Kits disabled.");
    }

    private void setupDatabase() {
        database = new Database(this);

        database.openConnection();

        this.cooldownsCache = new CooldownCache();
        KitCache kitsCache = new KitCache();

        this.cooldownRepository = new CooldownRepository(database, cooldownsCache);
        this.kitRepository = new KitRepository(database, kitsCache);
    }

    private void registerCommands() {
        getLogger().info("Registering commands...");

        registerCommand("createkit", new CreateKitCommand(kitRepository));
        registerCommand("deletekit", new DeleteKitCommand(cooldownRepository, kitRepository));
        registerCommand("editkit", new EditKitCommand(kitRepository));
        registerCommand("givekit", new GiveKitCommand(kitRepository));
        registerCommand("kit", new KitCommand(cooldownRepository, kitRepository));
        registerCommand("viewkit", new ViewKitCommand(kitRepository));
    }

    private void registerListeners() {
        getLogger().info("Registering listeners...");

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(kitRepository), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(cooldownsCache, cooldownRepository), this);
    }

    private void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = getCommand(commandName);

        if (command != null) {
            command.setExecutor(executor);
        } else {
            getLogger().severe("Failed to register the command: '" + commandName + "'.");
        }
    }
}