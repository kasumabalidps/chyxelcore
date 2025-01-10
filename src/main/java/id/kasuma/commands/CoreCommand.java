package id.kasuma.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import id.kasuma.Plugin;

public class CoreCommand implements CommandExecutor {
    private final Plugin plugin;

    public CoreCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chyxelcore.admin")) {
            sender.sendMessage(ChatColor.RED + "Kamu tidak memiliki izin untuk menggunakan command ini!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "ChyxelCore " + ChatColor.GRAY + "» " + 
                             ChatColor.WHITE + "Plugin berhasil di reload!");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "ChyxelCore " + ChatColor.GRAY + "» " + 
                         ChatColor.WHITE + "Gunakan: /chyxel reload");
        return true;
    }
} 