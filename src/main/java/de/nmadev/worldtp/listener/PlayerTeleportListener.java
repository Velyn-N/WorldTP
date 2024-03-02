package de.nmadev.worldtp.listener;

import de.nmadev.worldtp.ConfigManager;
import de.nmadev.worldtp.StorageManager;
import de.nmadev.worldtp.WorldTP;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class PlayerTeleportListener implements Listener {

    private final WorldTP worldTP;
    private final ConfigManager configManager;
    private final StorageManager storageManager;

    public PlayerTeleportListener(WorldTP worldTP, ConfigManager configManager, StorageManager storageManager) {
        this.worldTP = worldTP;
        this.configManager = configManager;
        this.storageManager = storageManager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.getPlayer().hasPermission(WorldTP.USE_PERMISSION)) {
            return;
        }

        Location fromLocation = event.getFrom();
        World toWorld = event.getTo().getWorld();

        if (fromLocation.getWorld().equals(toWorld)) {
            return;
        }

        handleWorldSwitch(fromLocation, toWorld, event.getPlayer(), event.getCause());
    }

    private void handleWorldSwitch(Location oldLoc, World enteredWorld, Player player, PlayerTeleportEvent.TeleportCause cause) {
        boolean isOldWorldActivated = configManager.isWorldActivated(oldLoc.getWorld());
        boolean isNewWorldActivated = configManager.isWorldActivated(enteredWorld);
        boolean isDefaultNetherPortalBehaviour = configManager.isDefaultNetherPortalBehaviour();

        if (!configManager.isBothWorldsActivatedOnly() || (isOldWorldActivated && isNewWorldActivated)) {
            storageManager.setLastPlayerLocation(player, oldLoc);
        }

        if (isDefaultNetherPortalBehaviour && cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            return;
        }

        if (isNewWorldActivated) {
            Optional<Location> lastPlayerLocation = storageManager.getLastPlayerLocation(player, enteredWorld);
            Bukkit.getScheduler().runTaskLater(worldTP, ()-> lastPlayerLocation.ifPresent(location -> player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN)), 1L);
        }
    }
}
