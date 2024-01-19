package de.nmadev.worldtp.command;

import de.nmadev.worldtp.ConfigManager;
import de.nmadev.worldtp.WorldTP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SwitchWorldCommand extends Command {

    private final ConfigManager configManager;

    public SwitchWorldCommand(ConfigManager configManager) {
        super("switchworld", "Switch between Worlds activated for WorldTP", "/switchworld <world>", new ArrayList<>());
        super.setPermission(WorldTP.SWITCH_WORLDS_PERMISSION);
        this.configManager = configManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(WorldTP.SWITCH_WORLDS_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have the permission for this Command.", NamedTextColor.RED));
            return false;
        }

        if (sender instanceof Player player) {
            World targetWorld = (args.length == 1) ? Bukkit.getWorld(args[0]) : null;
            if (targetWorld == null) {
                sender.sendMessage(Component.text("This Command only takes one Argument which has to be an activated World.", NamedTextColor.RED));
                return false;
            }
            player.teleport(targetWorld.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
            sender.sendMessage(Component.text("You have been teleported to ", NamedTextColor.GREEN)
                       .append(Component.text(targetWorld.getName(), NamedTextColor.DARK_GREEN))
                       .append(Component.text(".", NamedTextColor.GREEN)));
            return true;
        } else {
            sender.sendMessage(Component.text("This Command can only be used by Players.", NamedTextColor.RED));
            return false;
        }
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission(WorldTP.SWITCH_WORLDS_PERMISSION)) {
            return new ArrayList<>();
        }
        if (args.length > 1) {
            return new ArrayList<>();
        }
        String firstArg = (args.length > 0) ? args[0] : "";
        return configManager.getActivatedWorlds()
                .stream()
                .map(World::getName)
                .sorted(Comparator.comparingInt(w -> w.indexOf(firstArg)))
                .toList();
    }
}
