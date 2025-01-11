package id.kasuma;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import id.kasuma.listeners.PlayerConnectionListener;
import id.kasuma.listeners.PlayerWorldListener;
import id.kasuma.commands.CoreCommand;
import id.kasuma.commands.RTPCommand;
import id.kasuma.managers.LocationManager;
import id.kasuma.managers.MultiverseManager;
import id.kasuma.managers.TeleportManager;

public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("chyxelcore");
  private LocationManager locationManager;
  private MultiverseManager multiverseManager;
  private TeleportManager teleportManager;

  @Override
  public void onEnable()
  {
    // Save default config
    saveDefaultConfig();
    
    // Initialize managers
    multiverseManager = new MultiverseManager(this);
    locationManager = new LocationManager(this);
    teleportManager = new TeleportManager(this, locationManager);
    
    if (!multiverseManager.isEnabled()) {
      LOGGER.warning("Multiverse-Core not found! Some features may not work properly.");
    }
    
    // Event Listener lists
    getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    getServer().getPluginManager().registerEvents(new PlayerWorldListener(this, locationManager), this);
    
    // Register commands
    getCommand("chyxel").setExecutor(new CoreCommand(this));
    getCommand("rtp").setExecutor(new RTPCommand(this));
    
    LOGGER.info("ChyxelCore is loading...");
    LOGGER.info("ChyxelCore Successfully Enabled!");
  }

  @Override
  public void onDisable()
  {
    LOGGER.info("ChyxelCore Successfully Disabled!");
  }

  public MultiverseManager getMultiverseManager() {
    return multiverseManager;
  }

  public TeleportManager getTeleportManager() {
    return teleportManager;
  }
}
