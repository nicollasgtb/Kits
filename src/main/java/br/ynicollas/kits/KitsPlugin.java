package br.ynicollas.kits;

import br.ynicollas.kits.cache.CooldownsCache;
import br.ynicollas.kits.cache.KitsCache;
import br.ynicollas.kits.registry.CommandRegistry;
import br.ynicollas.kits.registry.ListenerRegistry;
import br.ynicollas.kits.storage.Database;
import br.ynicollas.kits.storage.cooldowns.CooldownsStorage;
import br.ynicollas.kits.storage.kits.KitsStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class KitsPlugin extends JavaPlugin {

    private Database database;

    private CooldownsStorage cooldowns;
    private KitsStorage kits;

    @Override
    public void onEnable() {
        initializeStorages();
        registerComponents();
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.closeConnection();
        }
    }

    private void initializeStorages() {
        database = new Database(this);

        database.openConnection();

        CooldownsCache cooldownsCache = new CooldownsCache();
        KitsCache kitsCache = new KitsCache();

        cooldowns = new CooldownsStorage(database, cooldownsCache);
        kits = new KitsStorage(database, kitsCache);
    }

    private void registerComponents() {
        new CommandRegistry(this, cooldowns, kits).registerCommands();
        new ListenerRegistry(this, kits).registerListeners();
    }
}