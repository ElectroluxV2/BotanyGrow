package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksScanned;
import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksToScan;

public class FloraPopulate extends BukkitRunnable {

    private static int random(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void run() {
        if (MainPlugin.blocksToPopulate.size() == 0) return;

        int randomIndex = random(0, MainPlugin.blocksToPopulate.size() - 1);
        Location l = MainPlugin.blocksToPopulate.get(randomIndex);
        MainPlugin.blocksToPopulate.remove(randomIndex);

        World w = l.getWorld();
        if (w == null) return;

        Chunk c = w.getChunkAt(l);
        if (!c.isLoaded()) return;

        // Block that is currently spreading
        Block o = w.getBlockAt(l);

        // Check for spots
        ArrayList<Block> acceptableBlocks = new ArrayList<>();
        int offsetX = 2, offsetZ = 2, offsetY = 1;

        for (int x = (int) (l.getX() - offsetX); x < l.getX() + offsetX; x++) {
            for (int z = (int) (l.getZ() - offsetZ); z < l.getZ() + offsetZ; z++) {
                for (int y = (int) (l.getY() - offsetY); y < l.getY() + offsetY; y++) {
                    Block test = w.getBlockAt(x, y, z);
                    if (!test.getChunk().isLoaded()) continue;

                    // Can spread on only free space
                    if (!test.getType().equals(Material.AIR)) continue;

                    Block blockUnder = w.getBlockAt(test.getLocation().subtract(0,1,0));

                    // Has to be place able
                    if (MainPlugin.settings.placeAbleMaterials.get(o.getType()) == null) continue;
                    if (!MainPlugin.settings.placeAbleMaterials.get(o.getType()).contains(blockUnder.getType())) continue;

                    acceptableBlocks.add(test);
                }
            }
        }

        // No spot
        if (acceptableBlocks.size() == 0) {
            return;
        }

        // Count neighbours
        HashMap<Material, Integer> neighbours = new HashMap<>();
        for (int x = l.getBlockX() - 1; x < l.getBlockX() + 1; x++) {
            for (int z = l.getBlockZ() - 1; z < l.getBlockZ() + 1; z++) {
                for (int y = l.getBlockY() - 1; y < l.getBlockY() + 1; y++) {
                    // Chunk have to be loaded in order to modify
                    Location n = new Location(w, x, y, z);
                    if (!w.getChunkAt(l).isLoaded()) continue;

                    Material m = w.getBlockAt(n).getType();
                    neighbours.merge(m, 1, Integer::sum);
                }
            }
        }

        // Rand new spot
        int indexForNewSpot = random(0, acceptableBlocks.size() - 1);
        Block target = acceptableBlocks.get(indexForNewSpot);

        // Grow mechanics
        // TODO: add chance to grow
        // TODO: other types than just grass
        if (o.getType().equals(Material.GRASS)) {

            Block upper = w.getBlockAt(o.getLocation().add(0,1,0));
            // Grow to tall grass if possible
            if (neighbours.get(Material.GRASS) >= 4 && upper.isEmpty()) {
                o.setType(Material.TALL_GRASS, false);
                Bisected lowerData = (Bisected) o.getBlockData();
                lowerData.setHalf(Bisected.Half.BOTTOM);
                o.setBlockData(lowerData);

                // Upper block has to be grass too
                upper.setType(Material.TALL_GRASS, false);
                Bisected upperData = (Bisected) upper.getBlockData();
                upperData.setHalf(Bisected.Half.TOP);
                upper.setBlockData(upperData);
            } else {
                // Just spread
                target.setType(Material.GRASS, false);
            }
        }

    }
}
