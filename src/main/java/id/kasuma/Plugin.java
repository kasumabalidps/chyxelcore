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
    try {
      initializePlugin();
      LOGGER.info("ChyxelCore Successfully Enabled!");
    } catch (Exception e) {
      LOGGER.severe("Failed to enable ChyxelCore: " + e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
    }
  }

  private void initializePlugin() {
    saveDefaultConfig();
    initializeManagers();
    registerListeners();
    registerCommands();
    LOGGER.info("ChyxelCore is loading...");
  }

  private void initializeManagers() {
    multiverseManager = new MultiverseManager(this);
    locationManager = new LocationManager(this);
    teleportManager = new TeleportManager(this, locationManager);

    if (!multiverseManager.isEnabled()) {
      LOGGER.warning("Multiverse-Core not found! Some features may not work properly.");
    }
  }

  private void registerListeners() {
    getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    getServer().getPluginManager().registerEvents(new PlayerWorldListener(this, locationManager), this);
  }

  private void registerCommands() {
    getCommand("chyxel").setExecutor(new CoreCommand(this));
    getCommand("rtp").setExecutor(new RTPCommand(this));
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
