package de.nmadev.worldtp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Objects;

public class ConfigManager {

    private static final String ACTIVATED_WORLDS = "activated-worlds";
    private static final String BOTH_ACTIVATED_WORLDS_ONLY = "both-worlds-activated-only";
    private static final String STORAGE_SAVE_INTERVAL = "storage-save-interval-minutes";
    public static final String SWITCH_WORLD_COMMAND_ACTIVE = "enable-switch-world-command";

    private final FileConfiguration config;

    private List<World> activatedWorlds;
    private Boolean isBothWorldsActivatedOnly;
    private Boolean isSwitchWorldCommandActivated;
    private Integer storageSaveInterval;

    public ConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void reloadConfig() {
        this.activatedWorlds = null;
        this.isBothWorldsActivatedOnly = null;
        this.isSwitchWorldCommandActivated = null;
        this.storageSaveInterval = null;
    }

    public List<World> getActivatedWorlds() {
        if (activatedWorlds == null) {
            activatedWorlds = config.getStringList(ACTIVATED_WORLDS).stream()
                    .map(Bukkit::getWorld)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return activatedWorlds;
    }

    public boolean isWorldActivated(World world) {
        return getActivatedWorlds().contains(world);
    }

    public boolean isBothWorldsActivatedOnly() {
        if (isBothWorldsActivatedOnly == null) {
            isBothWorldsActivatedOnly = config.getBoolean(BOTH_ACTIVATED_WORLDS_ONLY);
        }
        return isBothWorldsActivatedOnly;
    }

    public boolean isSwitchWorldCommandActivated() {
        if (isSwitchWorldCommandActivated == null) {
            isSwitchWorldCommandActivated = config.getBoolean(SWITCH_WORLD_COMMAND_ACTIVE);
        }
        return isSwitchWorldCommandActivated;
    }

    public int getStorageSaveInterval() {
        if (storageSaveInterval == null) {
            storageSaveInterval = config.getInt(STORAGE_SAVE_INTERVAL);
        }
        return storageSaveInterval;
    }
}
