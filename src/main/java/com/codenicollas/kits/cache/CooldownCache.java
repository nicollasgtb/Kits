package com.codenicollas.kits.cache;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownCache {

    private final Map<UUID, Map<String, Long>> cooldownsCache = new ConcurrentHashMap<>();

    public boolean hasCooldown(Player player, String kitId) {
        Map<String, Long> kitMap = cooldownsCache.get(player.getUniqueId());

        if (kitMap == null) {
            return false;
        }

        return kitMap.getOrDefault(kitId, 0L) > System.currentTimeMillis();
    }

    public long getCooldown(Player player, String kitId) {
        return cooldownsCache.getOrDefault(player.getUniqueId(), Collections.emptyMap())
                .getOrDefault(kitId, 0L);
    }

    public void addCooldown(Player player, String kitId, long expireTime) {
        cooldownsCache.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>())
                .put(kitId, expireTime);
    }

    public void loadCooldowns(Player player, Map<String, Long> loadedCooldowns) {
        if (loadedCooldowns == null || loadedCooldowns.isEmpty()) {
            return;
        }

        cooldownsCache.put(player.getUniqueId(), new ConcurrentHashMap<>(loadedCooldowns));
    }

    public void removeCooldown(Player player, String kitId) {
        cooldownsCache.computeIfPresent(player.getUniqueId(), (uuid, kitMap) -> {
            kitMap.remove(kitId);

            return kitMap.isEmpty() ? null : kitMap;
        });
    }

    public void removePlayer(Player player) {
        cooldownsCache.remove(player.getUniqueId());
    }

    public void removeCooldownsForKit(String kitId) {
        cooldownsCache.values().forEach(kitMap -> kitMap.remove(kitId));
    }
}