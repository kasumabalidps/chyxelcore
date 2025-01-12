package id.kasuma;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import id.kasuma.listeners.PlayerConnectionListener;
import id.kasuma.listeners.PlayerWorldListener;
import id.kasuma.commands.CoreCommand;
import id.kasuma.commands.RTPCommand;
import id.kasuma.commands.spawncommand.SetSpawnCommand;
import id.kasuma.commands.spawncommand.SpawnCommand;
import id.kasuma.managers.LocationManager;
import id.kasuma.managers.MultiverseManager;
import id.kasuma.managers.TeleportManager;
import id.kasuma.managers.DatabaseManager;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("chyxelcore");
  private LocationManager locationManager;
  private MultiverseManager multiverseManager;
  private TeleportManager teleportManager;
  private DatabaseManager databaseManager;
  private YamlConfiguration langConfig;

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
    loadLangConfig();
    initializeManagers();
    registerListeners();
    registerCommands();
    LOGGER.info("ChyxelCore is loading...");
  }

  private void initializeManagers() {
    databaseManager = new DatabaseManager(this);
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
    getCommand("spawn").setExecutor(new SpawnCommand(this));
    getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
  }

  private void loadLangConfig() {
    File langFile = new File(getDataFolder(), "lang.yml");
    if (!langFile.exists()) {
      saveResource("lang.yml", false);
    }
    langConfig = YamlConfiguration.loadConfiguration(langFile);
  }

  public String getLangMessage(String path) {
    return ChatColor.translateAlternateColorCodes('&', 
        langConfig.getString(path, "Message not found: " + path));
  }

  @Override
  public void onDisable()
  {
    if (databaseManager != null) {
      databaseManager.close();
    }
    LOGGER.info("ChyxelCore Successfully Disabled!");
  }

  public MultiverseManager getMultiverseManager() {
    return multiverseManager;
  }

  public TeleportManager getTeleportManager() {
    return teleportManager;
  }

  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }
}
