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
        createDataFolder();
    }

    private void createDataFolder() {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create player_locations directory!");
        }
    }

    public void savePlayerLocation(Player player, Location location) {
        if (!isValidSave(location.getWorld().getName())) return;

        UUID playerId = player.getUniqueId();
        HashMap<String, Location> playerLocations = lastLocations.computeIfAbsent(playerId, k -> new HashMap<>());
        playerLocations.put(location.getWorld().getName(), location.clone());

        saveAsync(player, playerLocations);
    }

    private boolean isValidSave(String worldName) {
        return plugin.getConfig().getBoolean("last-location.enabled", true) && 
               !plugin.getConfig().getStringList("last-location.blacklisted-worlds").contains(worldName);
    }

    private void saveAsync(Player player, HashMap<String, Location> locations) {
        new BukkitRunnable() {
            @Override
            public void run() {
                saveToFile(player.getUniqueId(), locations);
            }
        }.runTaskAsynchronously(plugin);
    }

    private void saveToFile(UUID playerId, HashMap<String, Location> locations) {
        try {
            YamlConfiguration config = new YamlConfiguration();
            locations.forEach((world, loc) -> config.set("locations." + world, loc));
            config.save(new File(dataFolder, playerId.toString() + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save location data: " + e.getMessage());
        }
    }

    public Location getLastLocation(Player player, String worldName) {
        HashMap<String, Location> locations = getPlayerLocations(player);
        Location loc = locations != null ? locations.get(worldName) : null;
        return loc != null ? loc.clone() : null;
    }

    private HashMap<String, Location> getPlayerLocations(Player player) {
        UUID playerId = player.getUniqueId();
        if (!lastLocations.containsKey(playerId)) {
            loadPlayerData(player);
        }
        return lastLocations.get(playerId);
    }

    private void loadPlayerData(Player player) {
        File file = new File(dataFolder, player.getUniqueId().toString() + ".yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        HashMap<String, Location> locations = new HashMap<>();
        
        if (config.contains("locations")) {
            config.getConfigurationSection("locations").getKeys(false)
                 .forEach(world -> locations.put(world, config.getLocation("locations." + world)));
        }
        
        lastLocations.put(player.getUniqueId(), locations);
    }

    public void clearPlayerData(UUID playerId) {
        lastLocations.remove(playerId);
        new File(dataFolder, playerId.toString() + ".yml").delete();
    }
} 