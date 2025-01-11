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
import java.util.EnumSet;
import java.util.Set;

public class PlayerWorldListener implements Listener {
    private final Plugin plugin;
    private final LocationManager locationManager;
    private final Set<PlayerTeleportEvent.TeleportCause> validCauses;

    public PlayerWorldListener(Plugin plugin, LocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
        this.validCauses = EnumSet.of(
            PlayerTeleportEvent.TeleportCause.COMMAND,
            PlayerTeleportEvent.TeleportCause.PLUGIN,
            PlayerTeleportEvent.TeleportCause.NETHER_PORTAL,
            PlayerTeleportEvent.TeleportCause.END_PORTAL
        );
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (shouldSaveOnDeath()) {
            locationManager.savePlayerLocation(event.getEntity(), event.getEntity().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (shouldSaveOnQuit()) {
            locationManager.savePlayerLocation(event.getPlayer(), event.getPlayer().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() || !validCauses.contains(event.getCause())) return;
        
        Location newLoc = plugin.getTeleportManager().handleTeleport(
            event.getPlayer(), event.getFrom(), event.getTo()
        );
        
        if (newLoc != null) {
            event.setTo(newLoc);
        }
    }

    private boolean shouldSaveOnDeath() {
        return plugin.getConfig().getBoolean("last-location.save-on.death", true);
    }

    private boolean shouldSaveOnQuit() {
        return plugin.getConfig().getBoolean("last-location.save-on.quit", true);
    }
} 