package br.ynicollas.kits.cache;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownsCache {

    private final Map<String, Map<String, Long>> cooldownsCache = new ConcurrentHashMap<>();

    public boolean hasCooldown(Player player, String id) {
        Map<String, Long> kitMap = cooldownsCache.get(player.getName());

        return kitMap != null && kitMap.getOrDefault(id, 0L) > System.currentTimeMillis();
    }

    public long getCooldown(Player player, String id) {
        return cooldownsCache.getOrDefault(player.getName(), Collections.emptyMap()).getOrDefault(id, 0L);
    }

    public void addCooldown(Player player, String id, long expireTime) {
        cooldownsCache.computeIfAbsent(player.getName(), key -> new ConcurrentHashMap<>())
                .put(id, expireTime);
    }

    public void removeCooldown(Player player, String id) {
        cooldownsCache.computeIfPresent(player.getName(), (name, kitMap) -> {
            kitMap.remove(id);
            return kitMap.isEmpty() ? null : kitMap;
        });
    }

    public void removePlayer(Player player) {
        cooldownsCache.remove(player.getName());
    }

    public void removeCooldownsForKit(String id) {
        cooldownsCache.values().forEach(kitMap -> kitMap.remove(id));
    }
}