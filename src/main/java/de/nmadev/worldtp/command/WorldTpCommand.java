package de.nmadev.worldtp.command;

import de.nmadev.worldtp.ConfigManager;
import de.nmadev.worldtp.StorageManager;
import de.nmadev.worldtp.WorldTP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldTpCommand extends Command {

    private final ConfigManager configManager;
    private final StorageManager storageManager;
    private final WorldTP worldTP;

    public WorldTpCommand(WorldTP worldTP, ConfigManager configManager, StorageManager storageManager) {
        super("worldtp", "The main command for worldtp", "/worldtp", new ArrayList<>());
        this.worldTP = worldTP;
        this.storageManager = storageManager;
        super.setPermission(WorldTP.ADMIN_PERMISSION);
        this.configManager = configManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(WorldTP.SWITCH_WORLDS_PERMISSION)) {
            sender.sendMessage(Component.text("You do not have the permission for this Command.", NamedTextColor.RED));
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("You need to specify a Subcommand.", NamedTextColor.RED));
            return false;
        }

        return switch (args[0]) {
            case "reload" -> {
                worldTP.reload().forEach(sender::sendMessage);
                yield true;
            }
            case "statistics" -> {
                StorageManager.Statistics stats = storageManager.getStatistics();
                sender.sendMessage(Component.text("--- WorldTP Plugin Statistics ---", NamedTextColor.GOLD)
                        .append(Component.newline())
                        .append(Component.text("Tracked Worlds: ", NamedTextColor.YELLOW))
                                         .append(Component.text(configManager.getActivatedWorlds().size())
                                         .append(Component.text(" (List)"))
                                         .hoverEvent(HoverEvent.showText(Component.text(configManager.getActivatedWorlds()
                                                                                  .stream()
                                                                                  .map(World::getName)
                                                                                  .collect(Collectors.joining(", "))))))
                        .append(Component.newline())
                        .append(getStatisticLine("Tracked Players", stats.getTrackedPlayers()))
                        .append(getStatisticLine("Locations saved", stats.getSavedLocations()))
                        .append(getStatisticLine("Saves triggered", stats.getSavesTriggeredCount()))
                );
                yield true;
            }
            default -> {
                sender.sendMessage(Component.text("Invalid Subcommand.", NamedTextColor.RED));
                yield false;
            }
        };
    }

    private Component getStatisticLine(String name, Object statistic) {
        return Component.text(name + ": ", NamedTextColor.YELLOW)
                        .append(Component.text(statistic.toString(), NamedTextColor.GOLD))
                        .append(Component.newline());
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!sender.hasPermission(WorldTP.ADMIN_PERMISSION)) {
            return new ArrayList<>();
        }
        return List.of("reload", "statistics");
    }
}
