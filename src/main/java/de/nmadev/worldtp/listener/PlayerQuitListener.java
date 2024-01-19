package de.nmadev.worldtp.listener;

import de.nmadev.worldtp.StorageManager;
import de.nmadev.worldtp.WorldTP;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final StorageManager storageManager;

    public PlayerQuitListener(StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().hasPermission(WorldTP.USE_PERMISSION)) {
            storageManager.setLastPlayerLocation(event.getPlayer(), event.getPlayer().getLocation());
        }
    }
}
