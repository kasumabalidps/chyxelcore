package id.kasuma.listeners;

import id.kasuma.Plugin;
import id.kasuma.managers.LocationManager;
import id.kasuma.managers.MultiverseManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerWorldListener implements Listener {
    private final Plugin plugin;
    private final LocationManager locationManager;
    private final MultiverseManager multiverseManager;

    public PlayerWorldListener(Plugin plugin, LocationManager locationManager, MultiverseManager multiverseManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
        this.multiverseManager = multiverseManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (!plugin.getConfig().getBoolean("last-location.save-on.world-change", true)) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();

        // Check if world is managed by Multiverse
        if (multiverseManager.isEnabled() && !multiverseManager.canEnterWorld(world)) {
            return;
        }

        locationManager.savePlayerLocation(player, player.getLocation());
        
        Location lastLoc = locationManager.getLastLocation(player, player.getWorld().getName());
        if (lastLoc != null) {
            player.teleport(lastLoc);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("last-location.save-on.quit", true)) {
            return;
        }

        Player player = event.getPlayer();
        locationManager.savePlayerLocation(player, player.getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!plugin.getConfig().getBoolean("last-location.save-on.teleport", true)) {
            return;
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN || 
            event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
            Player player = event.getPlayer();
            locationManager.savePlayerLocation(player, event.getFrom());
        }
    }
} 