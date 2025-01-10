package id.kasuma.managers;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiversePortals.MultiversePortals;
import id.kasuma.Plugin;
import org.bukkit.World;

public class MultiverseManager {
    private MultiverseCore mvCore;
    private MultiversePortals mvPortals;
    private MVWorldManager worldManager;
    private final Plugin plugin;

    public MultiverseManager(Plugin plugin) {
        this.plugin = plugin;
        setupMultiverse();
    }

    private void setupMultiverse() {
        org.bukkit.plugin.Plugin mvCorePlugin = plugin.getServer().getPluginManager().getPlugin("Multiverse-Core");
        org.bukkit.plugin.Plugin mvPortalsPlugin = plugin.getServer().getPluginManager().getPlugin("Multiverse-Portals");

        if (mvCorePlugin instanceof MultiverseCore) {
            mvCore = (MultiverseCore) mvCorePlugin;
            worldManager = mvCore.getMVWorldManager();
            plugin.getLogger().info("Successfully hooked into Multiverse-Core!");
        }

        if (mvPortalsPlugin instanceof MultiversePortals) {
            mvPortals = (MultiversePortals) mvPortalsPlugin;
            plugin.getLogger().info("Successfully hooked into Multiverse-Portals!");
        }
    }

    public boolean isMultiverseWorld(World world) {
        return worldManager != null && worldManager.isMVWorld(world);
    }

    public boolean canEnterWorld(World world) {
        return worldManager != null && worldManager.getMVWorld(world) != null;
    }

    public boolean isEnabled() {
        return mvCore != null;
    }

    public MultiverseCore getCore() {
        return mvCore;
    }

    public MultiversePortals getPortals() {
        return mvPortals;
    }
} 