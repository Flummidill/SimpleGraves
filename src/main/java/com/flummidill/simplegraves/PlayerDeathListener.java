package com.flummidill.simplegraves;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerDeathListener implements Listener {

    private final GraveManager graveManager;

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (player.hasPermission("simplegraves.use")) {
                graveManager.saveOfflinePlayer(player.getUniqueId(), player.getName());

                Location graveLocation = getValidGraveLocation(player.getLocation());
                if (graveLocation == null) {
                    player.sendMessage("§aIt's your Lucky Day!");
                    player.sendMessage("§cSimpleGraves was unable to place your Grave!");
                    player.sendMessage("§aBecause of this, you can keep your Items!");
                } else {
                    player.sendMessage("Your Grave is Located at: " + graveLocation.getBlockX() + ", " + graveLocation.getBlockY() + ", " + graveLocation.getBlockZ());
                    graveManager.createGrave(player, graveLocation);
                }
            }
        }
    }

    private Location getValidGraveLocation(Location graveLocation) {
        World world = graveLocation.getWorld();
        if (world == null) return null;

        int baseX = graveLocation.getBlockX();
        int baseY = graveLocation.getBlockY();
        int baseZ = graveLocation.getBlockZ();

        List<int[]> offsets = Arrays.asList(
                new int[]{0, 0},      // center
                new int[]{-1, 0},     // west
                new int[]{1, 0},      // east
                new int[]{0, -1},     // north
                new int[]{0, 1},      // south
                new int[]{-1, -1},    // northwest
                new int[]{1, -1},     // northeast
                new int[]{-1, 1},     // southwest
                new int[]{1, 1}       // southeast
        );

        List<Material> UNSAFE_BLOCKS = Arrays.asList(
                Material.WHITE_BED,
                Material.LIGHT_GRAY_BED,
                Material.GRAY_BED,
                Material.BLACK_BED,
                Material.BROWN_BED,
                Material.RED_BED,
                Material.ORANGE_BED,
                Material.YELLOW_BED,
                Material.LIME_BED,
                Material.GREEN_BED,
                Material.CYAN_BED,
                Material.LIGHT_BLUE_BED,
                Material.BLUE_BED,
                Material.PURPLE_BED,
                Material.MAGENTA_BED,
                Material.PINK_BED,
                Material.NOTE_BLOCK,
                Material.JUKEBOX,
                Material.ENCHANTING_TABLE,
                Material.GOLD_BLOCK,
                Material.IRON_BLOCK,
                Material.DIAMOND_BLOCK,
                Material.EMERALD_BLOCK,
                Material.LAPIS_BLOCK,
                Material.NETHERITE_BLOCK,
                Material.BARREL,
                Material.CHEST,
                Material.TRAPPED_CHEST,
                Material.DECORATED_POT,
                Material.ENDER_CHEST,
                Material.SHULKER_BOX,
                Material.WHITE_SHULKER_BOX,
                Material.LIGHT_GRAY_SHULKER_BOX,
                Material.GRAY_SHULKER_BOX,
                Material.BLACK_SHULKER_BOX,
                Material.BROWN_SHULKER_BOX,
                Material.RED_SHULKER_BOX,
                Material.ORANGE_SHULKER_BOX,
                Material.YELLOW_SHULKER_BOX,
                Material.LIME_SHULKER_BOX,
                Material.GREEN_SHULKER_BOX,
                Material.CYAN_SHULKER_BOX,
                Material.LIGHT_BLUE_SHULKER_BOX,
                Material.BLUE_SHULKER_BOX,
                Material.PURPLE_SHULKER_BOX,
                Material.MAGENTA_SHULKER_BOX,
                Material.PINK_SHULKER_BOX,
                Material.FURNACE,
                Material.BLAST_FURNACE,
                Material.SMOKER,
                Material.CAMPFIRE,
                Material.SOUL_CAMPFIRE,
                Material.BREWING_STAND,
                Material.PLAYER_HEAD,
                Material.PLAYER_WALL_HEAD,
                Material.ZOMBIE_HEAD,
                Material.ZOMBIE_WALL_HEAD,
                Material.CREEPER_HEAD,
                Material.CREEPER_WALL_HEAD,
                Material.SKELETON_SKULL,
                Material.SKELETON_WALL_SKULL,
                Material.WITHER_SKELETON_SKULL,
                Material.WITHER_SKELETON_WALL_SKULL,
                Material.PIGLIN_HEAD,
                Material.PIGLIN_WALL_HEAD,
                Material.DRAGON_HEAD,
                Material.DRAGON_WALL_HEAD,
                Material.HEAVY_CORE,
                Material.END_PORTAL,
                Material.END_PORTAL_FRAME,
                Material.END_GATEWAY,
                Material.DRAGON_EGG,
                Material.BEACON,
                Material.SPAWNER,
                Material.TRIAL_SPAWNER,
                Material.CREAKING_HEART,
                Material.NETHER_PORTAL,
                Material.OBSIDIAN,
                Material.BEDROCK,
                Material.COMMAND_BLOCK,
                Material.REPEATING_COMMAND_BLOCK,
                Material.CHAIN_COMMAND_BLOCK,
                Material.TEST_INSTANCE_BLOCK,
                Material.TEST_BLOCK,
                Material.LIGHT,
                Material.STRUCTURE_BLOCK,
                Material.JIGSAW,
                Material.BARRIER,
                Material.STRUCTURE_VOID
        );

        for (int[] offset : offsets) {
            int x = baseX + offset[0];
            int z = baseZ + offset[1];

            for (int y = baseY; y <= world.getMaxHeight(); y++) {
                Block block = world.getBlockAt(x, y, z);
                Material type = block.getType();

                if (UNSAFE_BLOCKS.contains(type)) continue;

                boolean isNearEndPortalFrame = false;
                for (int dx = -3; dx <= 3 && !isNearEndPortalFrame; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        Block checkBlock = world.getBlockAt(x + dx, y, z + dz);
                        if (checkBlock.getType() == Material.END_PORTAL_FRAME) {
                            isNearEndPortalFrame = true;
                            break;
                        }
                    }
                }

                if (isNearEndPortalFrame) continue;


                return new Location(world, x, y, z, graveLocation.getYaw(), graveLocation.getPitch());
            }
        }

        return null;
    }

    public PlayerDeathListener(GraveManager graveManager) {
        this.graveManager = graveManager;
    }
}