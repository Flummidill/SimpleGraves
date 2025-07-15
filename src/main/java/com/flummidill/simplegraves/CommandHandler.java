package com.flummidill.simplegraves;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandHandler implements CommandExecutor {

    private final GraveManager graveManager;

    public CommandHandler(GraveManager graveManager) {
        this.graveManager = graveManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly Players can run this Command!");

            return false;
        }

        Player player = (Player) sender;

        graveManager.saveOfflinePlayer(player.getUniqueId(), player.getName());

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "graveinfo":
                if (!player.hasPermission("simplegraves.use")) {
                    player.sendMessage("§cYou don’t have permission to use this command.");

                    return false;
                }

                if (!(args.length == 1)) {
                    player.sendMessage("Usage: /graveinfo <number>");

                    return false;
                }

                return  handleGraveInfo(player, args);

            case "graveadmin":
                if (!player.hasPermission("simplegraves.admin")) {
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


        if (!graveManager.graveExistsUUID(targetUUID, graveNumber)) {
            player.sendMessage("§cYou don't have a Grave with Number #" + graveNumber);
            return false;
        }

        Location graveLocation = graveManager.getGraveLocation(targetUUID, graveNumber);

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
        String subcommand = args[0].toLowerCase();

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /graveadmin " + subcommand + " <player> [<number>]");

            return false;
        }

        String targetName = args[1];
        int graveNumber = 1;

        if (args.length == 3) {
            try {
                graveNumber = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid grave number.");
                return false;
            }
        }

        UUID targetUUID;
        Player targetPlr = Bukkit.getPlayerExact(targetName);
        if (targetPlr != null) {
            targetUUID = targetPlr.getUniqueId();
        } else {
            targetUUID = graveManager.getOfflinePlayerUUID(targetName);
            if (targetUUID == null) {
                sender.sendMessage("§cPlayer '" + targetName + "' not found.");
                return false;
            }
        }

        switch (subcommand) {
            case "go":
                if (!graveManager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);
                    return false;
                }
                Location tpLoc = graveManager.getGraveLocation(targetUUID, graveNumber);
                if (tpLoc != null) {
                    sender.teleport(tpLoc);
                    sender.sendMessage("§aTeleported to " + targetName + "'s Grave #" + graveNumber);
                    return true;
                } else {
                    sender.sendMessage("§cFailed to retrieve Grave Location.");
                    return false;
                }

            case "list":
                List<String> graveList = graveManager.getGraveNumberList(targetUUID);
                if (graveList.isEmpty()) {
                    sender.sendMessage("§c" + graveManager.getOfflinePlayerName(targetUUID) + " currently no Graves.");
                } else {
                    sender.sendMessage("§a" + targetName + "'s Grave List:");
                    for (String graveNum : graveList) {
                        sender.sendMessage("§c#" + graveNum);
                    }
                }
                return true;

            case "info":
                if (!graveManager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with the number " + graveNumber + ".");
                    return false;
                }

                Location graveLocation = graveManager.getGraveLocation(targetUUID, graveNumber);

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
                if (!graveManager.graveExistsUUID(targetUUID, graveNumber)) {
                    sender.sendMessage("§c" + targetName + " doesn't have a Grave with Number #" + graveNumber);

                    return false;
                }

                graveManager.removeGrave(targetUUID, graveNumber);

                sender.sendMessage("§aRemoved " + targetName + "'s Grave #" + graveNumber);

                return true;

            default:
                sender.sendMessage("Usage: /graveadmin <go|list|info|remove> [<player>] [<number>]");

                return false;
        }
    }
}