package id.kasuma;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import id.kasuma.listeners.PlayerConnectionListener;
import id.kasuma.commands.CoreCommand;
import id.kasuma.commands.RTPCommand;

public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("chyxelcore");

  @Override
  public void onEnable()
  {
    // Save default config
    saveDefaultConfig();
    
    // Event Listener lists
    getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    
    // Register commands
    getCommand("chyxel").setExecutor(new CoreCommand(this));
    getCommand("rtp").setExecutor(new RTPCommand(this));
    
    LOGGER.info("chyxelcore enabled");
  }

  @Override
  public void onDisable()
  {
    LOGGER.info("chyxelcore disabled");
  }
}
