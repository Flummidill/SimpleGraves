package com.flummidill.simplegraves;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final GraveManager graveManager;
    public boolean graveStealing = true;

    @EventHandler
    public void onGraveBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getBlock().getLocation();

        graveManager.saveOfflinePlayer(player.getUniqueId(), player.getName());

        if (loc.getBlock().getType() == Material.PLAYER_HEAD) {
            if (graveManager.graveExistsLoc(loc)) {
                if (graveStealing && player.hasPermission("simplegraves.use")) {
                    graveManager.breakGrave(loc);
                } else {
                    if (graveManager.getGraveOwnerUUID(loc).equals(player.getUniqueId())) {
                        graveManager.breakGrave(loc);
                    } else {
                        player.sendMessage("§cYou cannot break other Player's Graves!");
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    public void disableGraveStealing() {
        this.graveStealing = false;
    }

    public BlockBreakListener(GraveManager graveManager) {
        this.graveManager = graveManager;
    }
}