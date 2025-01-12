package id.kasuma.commands.spawncommand;

import id.kasuma.Plugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.UUID;

public class SpawnCommand implements CommandExecutor {
    private final Plugin plugin;
    private final HashMap<UUID, Location> teleporting = new HashMap<>();
    private final int TELEPORT_DELAY = 5; // dalam detik

    public SpawnCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("spawn.messages.console-error"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chyxelcore.spawn")) {
            player.sendMessage(plugin.getLangMessage("spawn.messages.no-permission"));
            return true;
        }

        Location spawn = plugin.getDatabaseManager().getSpawnLocation();
        if (spawn == null) {
            player.sendMessage(plugin.getLangMessage("spawn.messages.not-set"));
            return true;
        }

        if (spawn.getWorld() == null) {
            player.sendMessage(plugin.getLangMessage("spawn.messages.world-not-found"));
            return true;
        }

        teleporting.put(player.getUniqueId(), player.getLocation());
        
        player.sendMessage(plugin.getLangMessage("spawn.messages.teleport-start")
            .replace("%time%", String.valueOf(TELEPORT_DELAY)));

        new BukkitRunnable() {
            int timeLeft = TELEPORT_DELAY;
            
            @Override
            public void run() {
                if (!teleporting.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }

                Location startLoc = teleporting.get(player.getUniqueId());
                Location currentLoc = player.getLocation();

                if (hasPlayerMoved(startLoc, currentLoc)) {
                    teleporting.remove(player.getUniqueId());
                    player.sendMessage(plugin.getLangMessage("spawn.messages.teleport-cancelled"));
                    this.cancel();
                    return;
                }

                if (timeLeft <= 0) {
                    teleporting.remove(player.getUniqueId());
                    player.teleport(spawn);
                    player.sendMessage(plugin.getLangMessage("spawn.messages.teleport-success"));
                    this.cancel();
                    return;
                }

                if (timeLeft <= 3) {
                    player.sendMessage(plugin.getLangMessage("spawn.messages.teleport-countdown")
                        .replace("%time%", String.valueOf(timeLeft)));
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    private boolean hasPlayerMoved(Location from, Location to) {
        return from.getWorld() != to.getWorld() ||
               from.getX() != to.getX() ||
               from.getY() != to.getY() ||
               from.getZ() != to.getZ();
    }
} 