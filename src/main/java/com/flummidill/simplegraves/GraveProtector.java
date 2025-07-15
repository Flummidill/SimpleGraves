package com.flummidill.simplegraves;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

public class GraveProtector implements Listener {

    private final GraveManager graveManager;

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isGraveBlock);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isGraveBlock);
    }


    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        Block block = event.getToBlock();

        if (isGraveBlock(block)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (isGraveBlock(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (isGraveBlock(block)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isGraveBlock(Block block) {
        if (block.getType() == Material.PLAYER_HEAD) {
            if (graveManager.graveExistsLoc(block.getLocation())) {
                return true;
            }
        }

        return false;
    }

    public GraveProtector(GraveManager graveManager) {
        this.graveManager = graveManager;
    }
}