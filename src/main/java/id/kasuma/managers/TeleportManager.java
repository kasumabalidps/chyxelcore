package id.kasuma.managers;

import id.kasuma.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class TeleportManager {
    private final Plugin plugin;
    private final LocationManager locationManager;

    public TeleportManager(Plugin plugin, LocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
    }

    public Location handleTeleport(Player player, Location from, Location to) {
        // Skip jika bukan perpindahan antar world
        if (from == null || to == null || from.getWorld().equals(to.getWorld())) {
            return null;
        }

        String fromWorld = from.getWorld().getName();
        String toWorld = to.getWorld().getName();

        // 1. Simpan lokasi world yang ditinggalkan
        if (!isWorldBlacklisted(fromWorld)) {
            locationManager.savePlayerLocation(player, from);
            sendSaveMessage(player, fromWorld);
        }

        // 2. Cek dan teleport ke lokasi tersimpan
        if (!isWorldBlacklisted(toWorld)) {
            Location lastLoc = locationManager.getLastLocation(player, toWorld);
            if (lastLoc != null) {
                return lastLoc;
            }
        }

        // 3. Gunakan lokasi default jika tidak ada lokasi tersimpan
        return to;
    }

    private boolean isWorldBlacklisted(String worldName) {
        return plugin.getConfig().getStringList("last-location.blacklisted-worlds")
                .contains(worldName);
    }

    private void sendSaveMessage(Player player, String worldName) {
        String message = plugin.getConfig().getString("last-location.messages.saved")
                .replace("%world%", worldName);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
            plugin.getConfig().getString("prefix_message") + message));
    }
} 