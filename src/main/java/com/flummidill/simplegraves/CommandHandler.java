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

            return false;
        }

        Player player = (Player) sender;

        manager.saveOfflinePlayer(player.getUniqueId(), player.getName());

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "graveinfo":
                if (!player.hasPermission("simplegraves.graveinfo")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");

                    return false;
                }

                if (!(args.length == 1)) {
                    player.sendMessage("Usage: /graveinfo <number>");

                    return false;
                }

                return  handleGraveInfo(player, args);

            case "graveadmin":
                if (!player.hasPermission("simplegraves.graveadmin.show")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");

                    return false;
                }

                if (args.length < 1 || args.length > 3) {
                    player.sendMessage("Usage: /graveadmin <go|list|info|delete> [<player>] [<number>]");
                    return false;
                }

                return handleGraveAdmin(player, args);

            default:
                return false;
        }
    }

    private boolean handleGraveInfo(Player player, String[] args) {
        UUID targetUUID = player.getUniqueId();
        int graveNumber = Integer.parseInt(args[0]);


        if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
            player.sendMessage("§cYou don't have a Grave with Number #" + graveNumber);
            return false;
        }

        Location graveLocation = manager.getGraveLocation(targetUUID, graveNumber);

        if (graveLocation == null || graveLocation.getWorld() == null) {
            player.sendMessage("§cFailed to retrieve the Grave Location");
            return false;
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
        String numberStr = args[2];

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
                return false;
        }

        int graveNumber = -1;
        if (args.length == 3) {
            if (numberStr.equals("*")) {
                if (!action.equals("remove")) {
                    sender.sendMessage("§cYou can only use Number * with the remove Command.");
                    return false;
                }
            } else {
                try {
                    graveNumber = Integer.parseInt(numberStr);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cGrave must be a Number.");
                    return false;
                }
            }
        } else {
            // No Number
        }

        UUID targetUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (targetName.equals("*")) {
            if (!action.equals("remove")) {
                sender.sendMessage("§cYou can only use Player * with the remove Command.");
                return false;
            }
        } else {
            Player target = Bukkit.getPlayerExact(targetName);
            if (target != null) {
                targetUUID = target.getUniqueId();
            } else if (manager.getOfflinePlayerUUID(targetName) != null) {
                targetUUID = manager.getOfflinePlayerUUID(targetName);
            } else {
                sender.sendMessage("§cPlayer '" + targetName + "' not found.");
                return false;
            }

            targetName = manager.getOfflinePlayerName(targetUUID);
        }

        switch (action) {
            case "go":
                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);
                    return false;
                }
                Location tpLoc = manager.getGraveLocation(targetUUID, graveNumber);
                if (tpLoc != null) {
                    sender.teleport(tpLoc);
                    sender.sendMessage("§aTeleported to " + targetName + "'s Grave #" + graveNumber);
                    return true;
                } else {
                    sender.sendMessage("§cFailed to retrieve Grave Location.");
                    return false;
                }

            case "list":
                List<String> graveList = manager.getGraveNumberList(targetUUID);
                if (graveList.isEmpty()) {
                    sender.sendMessage("§c" + manager.getOfflinePlayerName(targetUUID) + " currently no Graves.");
                } else {
                    sender.sendMessage("§a" + targetName + "'s Grave List:");
                    for (String graveNum : graveList) {
                        sender.sendMessage("§c#" + graveNum);
                    }
                }
                return true;

            case "info":
                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with the number " + graveNumber + ".");
                    return false;
                }

                Location graveLocation = manager.getGraveLocation(targetUUID, graveNumber);

                if (graveLocation == null || graveLocation.getWorld() == null) {
                    sender.sendMessage("§cFailed to retrieve the grave location or world.");
                    return false;
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
                if (!manager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);

                    return false;
                }

                manager.removeGrave(targetUUID, graveNumber);

                sender.sendMessage("§aRemoved " + targetName + "'s Grave #" + graveNumber);

                return true;

            default:
                sender.sendMessage("Usage: /graveadmin <go|list|info|remove> [<player>] [<number>]");

                return false;
        }
    }
}