package id.kasuma.managers;

import id.kasuma.Plugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LocationManager {
    private final Plugin plugin;
    private final File dataFolder;
    private final ConcurrentHashMap<UUID, HashMap<String, Location>> lastLocations;

    public LocationManager(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "player_locations");
        this.lastLocations = new ConcurrentHashMap<>();
        
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create player_locations directory!");
        }
    }

    public void savePlayerLocation(Player player, Location location) {
        if (!isLocationSaveEnabled(player, location)) return;

        UUID playerId = player.getUniqueId();
        HashMap<String, Location> playerLocations = lastLocations.computeIfAbsent(playerId, k -> new HashMap<>());
        playerLocations.put(location.getWorld().getName(), location.clone()); // Clone untuk keamanan

        // Save async dengan BukkitRunnable
        new BukkitRunnable() {
            @Override
            public void run() {
                saveToFile(player, playerLocations);
            }
        }.runTaskAsynchronously(plugin);
    }

    private boolean isLocationSaveEnabled(Player player, Location location) {
        if (!plugin.getConfig().getBoolean("last-location.enabled", true)) {
            return false;
        }

        String worldName = location.getWorld().getName();
        return !plugin.getConfig().getStringList("last-location.blacklisted-worlds").contains(worldName);
    }

    private void saveToFile(Player player, HashMap<String, Location> playerLocations) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        for (String world : playerLocations.keySet()) {
            config.set("locations." + world, playerLocations.get(world));
        }
        
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save location for player: " + player.getName());
            e.printStackTrace();
        }
    }

    public Location getLastLocation(Player player, String worldName) {
        UUID playerId = player.getUniqueId();
        if (!lastLocations.containsKey(playerId)) {
            loadPlayerData(player);
        }
        
        HashMap<String, Location> playerLocations = lastLocations.get(playerId);
        Location loc = playerLocations != null ? playerLocations.get(worldName) : null;
        return loc != null ? loc.clone() : null; // Clone untuk keamanan
    }

    private void loadPlayerData(Player player) {
        File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        if (!playerFile.exists()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        HashMap<String, Location> locations = new HashMap<>();
        
        if (config.contains("locations")) {
            for (String world : config.getConfigurationSection("locations").getKeys(false)) {
                locations.put(world, config.getLocation("locations." + world));
            }
        }
        
        lastLocations.put(player.getUniqueId(), locations);
    }

    public void clearPlayerData(UUID playerId) {
        lastLocations.remove(playerId);
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        if (playerFile.exists()) {
            playerFile.delete();
        }
    }
} 