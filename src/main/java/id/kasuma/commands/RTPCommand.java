package id.kasuma.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Random;
import java.util.HashMap;
import java.util.UUID;
import id.kasuma.Plugin;

public class RTPCommand implements CommandExecutor {
    private final Plugin plugin;
    private final Random random = new Random();
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public RTPCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(colorize(getPrefix() + "Command ini hanya bisa digunakan oleh player!"));
            return true;
        }

        Player player = (Player) sender;
        World world = player.getWorld();
        String worldName = world.getName();

        // Check if world is enabled
        if (!plugin.getConfig().getBoolean("rtp.enabled-worlds." + worldName + ".enabled", false)) {
            player.sendMessage(colorize(getPrefix() + plugin.getConfig().getString("rtp.messages.wrong-world")));
            return true;
        }

        // Check cooldown
        if (hasCooldown(player)) {
            long timeLeft = getCooldownTime(player);
            String message = plugin.getConfig().getString("rtp.messages.cooldown")
                    .replace("%time%", String.valueOf(timeLeft));
            player.sendMessage(colorize(getPrefix() + message));
            return true;
        }

        player.sendMessage(colorize(getPrefix() + plugin.getConfig().getString("rtp.messages.searching")));

        // Cari lokasi secara async
        new BukkitRunnable() {
            @Override
            public void run() {
                Location safeLoc = findSafeLocation(player);
                
                // Teleport secara sync
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (safeLoc != null) {
                            player.teleport(safeLoc);
                            player.sendMessage(colorize(getPrefix() + plugin.getConfig().getString("rtp.messages.success")));
                            setCooldown(player);
                        } else {
                            player.sendMessage(colorize(getPrefix() + plugin.getConfig().getString("rtp.messages.failed")));
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);

        return true;
    }

    private Location findSafeLocation(Player player) {
        World world = player.getWorld();
        String worldName = world.getName();
        int maxRadius = plugin.getConfig().getInt("rtp.enabled-worlds." + worldName + ".max-radius", 10000);
        int minY = plugin.getConfig().getInt("rtp.enabled-worlds." + worldName + ".min-y", 62);
        int maxY = plugin.getConfig().getInt("rtp.enabled-worlds." + worldName + ".max-y", 255);

        for (int attempts = 0; attempts < 10; attempts++) {
            int x = random.nextInt(maxRadius * 2) - maxRadius;
            int z = random.nextInt(maxRadius * 2) - maxRadius;

            Location loc = new Location(world, x, 0, z);
            int highestY = world.getHighestBlockYAt(loc);

            if (highestY < minY || highestY > maxY) {
                continue;
            }

            loc.setY(highestY + 1);
            Block block = loc.getBlock();
            Block below = block.getRelative(0, -1, 0);
            Block above = block.getRelative(0, 1, 0);

            if (isSafeLocation(below, block, above)) {
                return loc.add(0.5, 0, 0.5); // Center on block
            }
        }

        return null;
    }

    private boolean isSafeLocation(Block below, Block block, Block above) {
        return !below.isLiquid() && 
               !below.isEmpty() && 
               !block.getType().isSolid() && 
               !above.getType().isSolid();
    }

    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean hasCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        return getCooldownTime(player) > 0;
    }

    private long getCooldownTime(Player player) {
        long cooldownTime = plugin.getConfig().getLong("rtp.cooldown", 300) * 1000;
        long lastUsage = cooldowns.get(player.getUniqueId());
        long remainingTime = (lastUsage + cooldownTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remainingTime);
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String getPrefix() {
        return plugin.getConfig().getString("prefix_message", "&8[&6ChyxelCore&8] &7");
    }
} 