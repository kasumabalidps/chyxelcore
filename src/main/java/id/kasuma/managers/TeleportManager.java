package id.kasuma.managers;

import id.kasuma.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TeleportManager {
    private final Plugin plugin;
    private final LocationManager locationManager;
    private final String prefix;

    public TeleportManager(Plugin plugin, LocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
        this.prefix = plugin.getConfig().getString("prefix_message", "");
    }

    public Location handleTeleport(Player player, Location from, Location to) {
        if (!isValidTeleport(from, to)) return null;

        String fromWorld = from.getWorld().getName();
        String toWorld = to.getWorld().getName();

        handleFromWorld(player, from, fromWorld);
        return handleToWorld(player, to, toWorld);
    }

    private boolean isValidTeleport(Location from, Location to) {
        return from != null && to != null && !from.getWorld().equals(to.getWorld());
    }

    private void handleFromWorld(Player player, Location from, String worldName) {
        if (!isWorldBlacklisted(worldName)) {
            locationManager.savePlayerLocation(player, from);
            sendSaveMessage(player, worldName);
        }
    }

    private Location handleToWorld(Player player, Location to, String worldName) {
        if (isWorldBlacklisted(worldName)) return to;
        
        Location lastLoc = locationManager.getLastLocation(player, worldName);
        return lastLoc != null ? lastLoc : to;
    }

    private boolean isWorldBlacklisted(String worldName) {
        return plugin.getConfig().getStringList("last-location.blacklisted-worlds")
                .contains(worldName);
    }

    private void sendSaveMessage(Player player, String worldName) {
        String message = plugin.getConfig().getString("last-location.messages.saved", "")
                .replace("%world%", worldName);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
} 