package de.nmadev.worldtp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class StorageManager {

    private final FileConfiguration config;
    private final Runnable saveAction;
    private final Logger log;

    private final Map<UUID, Map<String, Location>> playerWorldLocations = new HashMap<>();
    private Map<String, World> worldTempCache;
    private final AtomicInteger saveTriggerCounter = new AtomicInteger();

    public StorageManager(FileConfiguration storageConfig, Runnable saveAction, Logger log) {
        this.config = storageConfig;
        this.saveAction = saveAction;
        this.log = log;
    }

    public void readStorageFromFile() {
        Set<String> playerKeys = config.getKeys(false);
        for (String playerKey : playerKeys) {
            try {
                UUID uuid = UUID.fromString(playerKey);
                Map<String, Location> worldLocations = playerWorldLocations.computeIfAbsent(uuid, (u) -> new HashMap<>());
                ConfigurationSection playerSection = config.getConfigurationSection(playerKey);

                if (playerSection == null) {
                    continue;
                }

                playerSection.getKeys(false).stream()
                        .map(this::getWorldByNameCached)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(world -> getWorldLocationFromPlayerSection(playerSection, world))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(loc -> worldLocations.put(loc.getWorld().getName(), loc));
            } catch (IllegalArgumentException e) {
                log.info("Invalid UUID found in storage.yml: " + playerKey);
            }
        }
        if (worldTempCache != null) {
            worldTempCache.clear();
        }
    }

    private Optional<World> getWorldByNameCached(String worldName) {
        if (worldTempCache == null) {
            worldTempCache = new HashMap<>();
        }
        return Optional.ofNullable(worldTempCache.computeIfAbsent(worldName, Bukkit::getWorld));
    }

    private Optional<Location> getWorldLocationFromPlayerSection(ConfigurationSection playerSection, World world) {
        ConfigurationSection worldSection = playerSection.getConfigurationSection(world.getName());
        if (worldSection == null) {
            return Optional.empty();
        }
        double x = worldSection.getDouble("x");
        double y = worldSection.getDouble("y");
        double z = worldSection.getDouble("z");
        return Optional.of(new Location(world, x, y, z));
    }

    public void writeStorageToFile() {
        saveTriggerCounter.incrementAndGet();
        playerWorldLocations.forEach((uuid, worldLocations) -> {
            ConfigurationSection playerSection = getOrCreateConfigSection(config, uuid.toString());

            worldLocations.forEach((worldName, location) -> {
                ConfigurationSection worldSection = getOrCreateConfigSection(playerSection, worldName);
                worldSection.set("x", location.x());
                worldSection.set("y", location.y());
                worldSection.set("z", location.z());
            });
        });
        saveAction.run();
    }

    private ConfigurationSection getOrCreateConfigSection(ConfigurationSection config, String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            return section;
        }
        return config.createSection(path);
    }

    public Optional<Location> getLastPlayerLocation(Player player, World world) {
        return Optional.ofNullable(playerWorldLocations.computeIfAbsent(player.getUniqueId(), (u) -> new HashMap<>())
                                                       .get(world.getName()));
    }

    public void setLastPlayerLocation(Player player, Location location) {
        playerWorldLocations.computeIfAbsent(player.getUniqueId(), (u) -> new HashMap<>())
                            .put(location.getWorld().getName(), location);
    }

    public Statistics getStatistics() {
        return new Statistics();
    }

    public class Statistics {
        public int getTrackedPlayers() {
            return playerWorldLocations.size();
        }

        public int getSavedLocations() {
            return playerWorldLocations.values().stream().mapToInt(Map::size).sum();
        }

        public int getSavesTriggeredCount() {
            return saveTriggerCounter.get();
        }
    }
}
