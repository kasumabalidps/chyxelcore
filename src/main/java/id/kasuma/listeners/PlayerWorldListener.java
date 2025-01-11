package id.kasuma.listeners;

import id.kasuma.Plugin;
import id.kasuma.managers.LocationManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerWorldListener implements Listener {
    private final Plugin plugin;
    private final LocationManager locationManager;

    public PlayerWorldListener(Plugin plugin, LocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("last-location.save-on.death", true)) {
            locationManager.savePlayerLocation(event.getEntity(), event.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (plugin.getConfig().getBoolean("last-location.save-on.quit", true)) {
            locationManager.savePlayerLocation(event.getPlayer(), event.getPlayer().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (isValidTeleportCause(cause)) {
            Location newLoc = plugin.getTeleportManager().handleTeleport(
                event.getPlayer(), event.getFrom(), event.getTo()
            );
            
            if (newLoc != null) {
                event.setTo(newLoc);
            }
        }
    }

    private boolean isValidTeleportCause(PlayerTeleportEvent.TeleportCause cause) {
        return cause == PlayerTeleportEvent.TeleportCause.COMMAND || 
               cause == PlayerTeleportEvent.TeleportCause.PLUGIN ||
               cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ||
               cause == PlayerTeleportEvent.TeleportCause.END_PORTAL;
    }
} 