package com.codenicollas.kits.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.codenicollas.kits.cache.CooldownCache;
import com.codenicollas.kits.storage.repositories.CooldownRepository;

public class PlayerSessionListener implements Listener {

    private final CooldownCache cooldownCache;
    private final CooldownRepository cooldownRepository;

    public PlayerSessionListener(CooldownCache cooldownCache, CooldownRepository cooldownRepository) {
        this.cooldownCache = cooldownCache;
        this.cooldownRepository = cooldownRepository;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        cooldownRepository.loadCooldowns(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        cooldownCache.removePlayer(player);
    }
}