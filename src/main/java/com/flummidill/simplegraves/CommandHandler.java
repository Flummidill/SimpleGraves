package com.flummidill.simplegraves;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;


public class CommandHandler implements CommandExecutor {

    private final SimpleGraves plugin;
    private final GraveManager manager;


    public CommandHandler(SimpleGraves plugin, GraveManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly Players can run this Command!");

            return true;
        }

        Player player = (Player) sender;

        manager.saveOfflinePlayer(player.getUniqueId(), player.getName());

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "graveinfo":
                if (!player.hasPermission("simplegraves.graveinfo")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");

                    return true;
                }

                if (!(args.length == 1)) {
                    player.sendMessage("Usage: /graveinfo <number>");

                    return true;
                }

                return  handleGraveInfo(player, args);

            case "graveadmin":
                if (!player.hasPermission("simplegraves.graveadmin.show")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");

                    return true;
                }

                if (!(args.length == 2 || args.length == 3)) {
                    player.sendMessage("Usage: /graveadmin <go|list|info|delete> [<player>] [<number>]");
                    return true;
                }

                return handleGraveAdmin(player, args);

            default:
                player.sendMessage("Usage: /graveadmin <go|list|info|delete> [<player>] [<number>]");
                return true;
        }
    }

    private boolean handleGraveInfo(Player player, String[] args) {
        UUID targetUUID = player.getUniqueId();
        int graveNumber;

        try {
            graveNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cGrave must be a Number.");
            return true;
        }

        if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
            player.sendMessage("§cYou don't have a Grave with Number #" + graveNumber);
            return true;
        }

        Location graveLocation = manager.getGraveLocation(targetUUID, graveNumber);

        if (graveLocation == null || graveLocation.getWorld() == null) {
            player.sendMessage("§cFailed to retrieve the Grave Location");
            return true;
        }

        String worldName = "The Overworld";

        switch (graveLocation.getWorld().getName()) {
            case "world":
                worldName = "The Overworld";
                break;
            case "world_nether":
                worldName = "The Nether";
                break;
            case "world_the_end":
                worldName = "The End";
        }

        player.sendMessage("§aGrave #" + graveNumber + " is Located at:" +
                "\n§9World: §c" + worldName +
                "\n§9X: §c" + Math.floor(graveLocation.getX()) +
                "\n§9Y: §c" + Math.floor(graveLocation.getY()) +
                "\n§9Z: §c" + Math.floor(graveLocation.getZ()));

        return true;
    }

    private boolean handleGraveAdmin(Player sender, String[] args) {
        String action = args[0].toLowerCase();
        String targetName = args[1];
        String numberStr = "-1";

        switch (action) {
            case "go":
                if (!sender.hasPermission("simplegraves.graveadmin.go")) {
                    sender.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                break;

            case "list":
                if (!sender.hasPermission("simplegraves.graveadmin.list")) {
                    sender.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                break;

            case "info":
                if (!sender.hasPermission("simplegraves.graveadmin.info")) {
                    sender.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                break;

            case "remove":
                if (!sender.hasPermission("simplegraves.graveadmin.remove")) {
                    sender.sendMessage("§cYou don’t have permission to use this command.");
                    return true;
                }
                break;

            default:
                sender.sendMessage("Usage: /graveadmin <go|list|info|remove> [<player>] [<number>]");
                return true;
        }

        UUID targetUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (targetName.equals("*")) {
            if (!action.equals("remove")) {
                sender.sendMessage("§cYou can only use Player * with the remove Command.");
                return true;
            }
        } else {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                targetUUID = target.getUniqueId();
            } else if (manager.getOfflinePlayerUUID(targetName) != null) {
                targetUUID = manager.getOfflinePlayerUUID(targetName);
            } else {
                sender.sendMessage("§cPlayer '" + targetName + "' not found.");
                return true;
            }

            targetName = manager.getOfflinePlayerName(targetUUID);
        }

        int graveNumber = -1;
        if (args.length == 3) {
            numberStr = args[2];

            if (numberStr.equals("*")) {
                if (!action.equals("remove")) {
                    sender.sendMessage("§cYou can only use Number * with the remove Command.");
                    return true;
                }
            } else {
                try {
                    graveNumber = Integer.parseInt(numberStr);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cGrave must be a Number.");
                    return true;
                }
            }
        }

        if (args.length == 2 && !action.equals("list")) {
            sender.sendMessage("§cPlease specify a Grave Number.");
            return true;
        }

        if (graveNumber == -1 && !(action.equals("remove") || action.equals("list"))) {
            sender.sendMessage("§cYou can only use Number * with the remove Command.");
            return true;
        }

        switch (action) {
            case "go":
                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);
                    return true;
                }
                Location tpLoc = manager.getGraveLocation(targetUUID, graveNumber);
                if (tpLoc != null) {
                    sender.teleport(tpLoc);
                    sender.sendMessage("§aTeleported to " + targetName + "'s Grave #" + graveNumber);
                    return true;
                } else {
                    sender.sendMessage("§cFailed to retrieve Grave Location.");
                    return true;
                }

            case "list":
                List<String> graveList = manager.getGraveNumberList(targetUUID);
                if (graveList.isEmpty()) {
                    sender.sendMessage("§c" + manager.getOfflinePlayerName(targetUUID) + " currently has no Graves.");
                } else {
                    sender.sendMessage("§a" + targetName + "'s Grave List:");
                    for (String graveNum : graveList) {
                        sender.sendMessage("§c#" + graveNum);
                    }
                }
                return true;

            case "info":
                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber + ".");
                    return true;
                }

                Location graveLocation = manager.getGraveLocation(targetUUID, graveNumber);

                if (graveLocation == null || graveLocation.getWorld() == null) {
                    sender.sendMessage("§cFailed to retrieve the grave location or world.");
                    return true;
                }

                String worldName = "The Overworld";

                switch (graveLocation.getWorld().getName()) {
                    case "world":
                        worldName = "The Overworld";
                        break;
                    case "world_nether":
                        worldName = "The Nether";
                        break;
                    case "world_the_end":
                        worldName = "The End";
                }

                sender.sendMessage("§a" + targetName + "'s Grave #" + graveNumber + " is Located at:" +
                        "\n§9World: §c" + worldName +
                        "\n§9X: §c" + Math.floor(graveLocation.getX()) +
                        "\n§9Y: §c" + Math.floor(graveLocation.getY()) +
                        "\n§9Z: §c" + Math.floor(graveLocation.getZ()));


                return true;

            case "remove":
                if (targetName.equals("*")) {
                    if (numberStr.equals("*")) {
                        manager.removeEveryGrave();
                        sender.sendMessage("§aRemoved all Graves of all Players.");
                    } else {
                        manager.removeAllGravesWithNumber(graveNumber);
                        sender.sendMessage("§aRemoved all Graves with Number #" + graveNumber + ".");
                    }
                    break;
                }

                if (numberStr.equals("*")) {
                    manager.removeAllGraves(targetUUID);
                    sender.sendMessage("§aRemoved all Graves of " + targetName + ".");
                    break;
                }

                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);

                    return true;
                }
                manager.removeGrave(targetUUID, graveNumber);
                sender.sendMessage("§aRemoved " + targetName + "'s Grave #" + graveNumber);
                return true;

            default:
                sender.sendMessage("Usage: /graveadmin <go|list|info|remove> [<player>] [<number>]");

                return true;
        }
        return true;
    }
}