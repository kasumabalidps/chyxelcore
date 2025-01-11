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
        String worldName = location.getWorld().getName();
        
        plugin.getLogger().info("=== SAVE LOCATION DEBUG ===");
        plugin.getLogger().info("Player: " + player.getName());
        plugin.getLogger().info("World: " + worldName);
        plugin.getLogger().info("Location: " + formatLocation(location));
        plugin.getLogger().info("Blacklisted: " + plugin.getConfig().getStringList("last-location.blacklisted-worlds").contains(worldName));
        plugin.getLogger().info("Enabled: " + plugin.getConfig().getBoolean("last-location.enabled", true));

        // Cek apakah world di blacklist
        if (plugin.getConfig().getStringList("last-location.blacklisted-worlds").contains(worldName)) {
            plugin.getLogger().info("Skipping save - World is blacklisted");
            return;
        }

        // Cek apakah fitur diaktifkan
        if (!plugin.getConfig().getBoolean("last-location.enabled", true)) {
            plugin.getLogger().info("Skipping save - Feature is disabled");
            return;
        }

        UUID playerId = player.getUniqueId();
        HashMap<String, Location> playerLocations = lastLocations.computeIfAbsent(playerId, k -> new HashMap<>());
        playerLocations.put(worldName, location.clone());
        plugin.getLogger().info("Location saved to memory cache");

        // Save async dengan BukkitRunnable
        new BukkitRunnable() {
            @Override
            public void run() {
                saveToFile(player, playerLocations);
                plugin.getLogger().info("Location saved to file");
            }
        }.runTaskAsynchronously(plugin);
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

    private String formatLocation(Location loc) {
        return String.format("x:%.2f y:%.2f z:%.2f yaw:%.2f pitch:%.2f", 
            loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
} 