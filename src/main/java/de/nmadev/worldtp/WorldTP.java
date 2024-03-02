package de.nmadev.worldtp;

import de.nmadev.worldtp.command.SwitchWorldCommand;
import de.nmadev.worldtp.command.WorldTpCommand;
import de.nmadev.worldtp.listener.PlayerTeleportListener;
import de.nmadev.worldtp.listener.PlayerQuitListener;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WorldTP extends JavaPlugin {
    public static final String USE_PERMISSION = "worldtp.use";
    public static final String SWITCH_WORLDS_PERMISSION = "worldtp.switchworlds";
    public static final String ADMIN_PERMISSION = "worldtp.admin";

    private Logger log;

    private File storageFile;
    private StorageManager storageManager;
    private ConfigManager configManager;
    private ScheduledTask storageSaveTask;

    @Override
    public void onEnable() {
        super.onEnable();
        log = getLogger();

        saveDefaultConfig();
        configManager = new ConfigManager(getConfig());

        FileConfiguration storageConfig = getOrCreateStorageConfig();
        storageManager = new StorageManager(storageConfig, () -> {
            try {
                storageConfig.save(storageFile);
            } catch (IOException e) {
                log.severe("Error saving StorageFile: " + e.getMessage());
            }
        }, log);
        storageManager.readStorageFromFile();

        createStorageSaveTask();

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(storageManager), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this, configManager, storageManager), this);

        getServer().getCommandMap().register(getName(), new WorldTpCommand(this, configManager, storageManager));
        if (configManager.isSwitchWorldCommandActivated()) {
            getServer().getCommandMap().register(getName(), new SwitchWorldCommand(configManager));
        }

        log.info(getName() + " enabled!");
    }

    private void createStorageSaveTask() {
        long delay = configManager.getStorageSaveInterval();
        storageSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(this, (task) -> storageManager.writeStorageToFile(), delay, delay, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        storageManager.writeStorageToFile();
        log.info(getName() + " disabled.");
    }

    /**
     * Reloads the plugin. Returns a List of messages with errors, hints or information that should be forwarded to the sender.
     * @return A list of messages for the sender
     */
    public List<Component> reload() {
        List<Component> messages = new ArrayList<>();

        storageSaveTask.cancel();
        storageManager.writeStorageToFile();
        storageManager.readStorageFromFile();
        createStorageSaveTask();

        boolean wasSwitchCommandActive = configManager.isSwitchWorldCommandActivated();
        
        this.reloadConfig();
        configManager.reloadConfig();
        if (wasSwitchCommandActive != configManager.isSwitchWorldCommandActivated()) {
            if (configManager.isSwitchWorldCommandActivated()) {
                getServer().getCommandMap().register(getName(), new SwitchWorldCommand(configManager));
            } else {
                messages.add(Component.text("The ", NamedTextColor.RED)
                     .append(Component.text(ConfigManager.SWITCH_WORLD_COMMAND_ACTIVE, NamedTextColor.DARK_RED))
                     .append(Component.text(" setting can only be changed with a Server Restart.", NamedTextColor.RED))
                     .append(Component.newline())
                     .append(Component.text("The functionality has been disabled but the Command is still available.", NamedTextColor.RED)));
            }
        }
        messages.add(Component.text("Reload successful!", NamedTextColor.GREEN));
        return messages;
    }

    private FileConfiguration getOrCreateStorageConfig() {
        storageFile = new File(getDataFolder(), "storage.yml");
        if (!storageFile.exists()) {
            boolean dirCreated = storageFile.getParentFile().mkdirs();
            if (dirCreated) {
                saveResource("storage.yml", false);
            }
        }
        return YamlConfiguration.loadConfiguration(storageFile);
    }
}
