package id.kasuma.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.Location;
import id.kasuma.Plugin;

public class PlayerConnectionListener implements Listener {
    private final Plugin plugin;
    
    public PlayerConnectionListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!player.hasPlayedBefore()) {
            String firstJoinMessage = plugin.getConfig().getString("messages.first-join", "");
            firstJoinMessage = firstJoinMessage.replace("%player%", player.getName());
            event.setJoinMessage(colorize(firstJoinMessage));
            
            Location spawnLocation = new Location(
                Bukkit.getWorld(plugin.getConfig().getString("spawn.world", "world")),
                plugin.getConfig().getDouble("spawn.x", 0),
                plugin.getConfig().getDouble("spawn.y", 64),
                plugin.getConfig().getDouble("spawn.z", 0),
                (float) plugin.getConfig().getDouble("spawn.yaw", 0),
                (float) plugin.getConfig().getDouble("spawn.pitch", 0)
            );
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.teleport(spawnLocation);
            }, 5L);
            
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
            }
        } else {
            String joinMessage = plugin.getConfig().getString("messages.join", "");
            joinMessage = joinMessage.replace("%player%", player.getName());
            event.setJoinMessage(colorize(joinMessage));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String quitMessage = plugin.getConfig().getString("messages.quit", "");
        quitMessage = quitMessage.replace("%player%", player.getName());
        event.setQuitMessage(colorize(quitMessage));
    }
} 