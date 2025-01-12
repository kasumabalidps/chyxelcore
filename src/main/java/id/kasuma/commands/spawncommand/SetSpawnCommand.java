package id.kasuma.commands.spawncommand;

import id.kasuma.Plugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements CommandExecutor {
    private final Plugin plugin;

    public SetSpawnCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLangMessage("setspawn.messages.console-error"));
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("chyxelcore.admin")) {
            player.sendMessage(plugin.getLangMessage("setspawn.messages.no-permission"));
            return true;
        }

        Location loc = player.getLocation();
        plugin.getDatabaseManager().setSpawnLocation(loc);
        player.sendMessage(plugin.getLangMessage("setspawn.messages.success"));
        return true;
    }
} 