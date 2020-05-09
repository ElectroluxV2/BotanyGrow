package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Random;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksScanned;
import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksToScan;

public class FloraPopulate extends BukkitRunnable {

    private static int random(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void run() {
        MainPlugin.logger.info("Startttt");
        if (MainPlugin.blocksToPopulate.size() == 0) return;

        int randomIndex = random(0, MainPlugin.blocksToPopulate.size() - 1);
        Location l = MainPlugin.blocksToPopulate.get(randomIndex);
        MainPlugin.blocksToPopulate.remove(randomIndex);

        World w = l.getWorld();
        if (w == null) return;

        Chunk c = w.getChunkAt(l);
        if (!c.isLoaded()) return;

        ArrayList<Block> acceptableBlocks = new ArrayList<Block>();
        int offsetX = 3, offsetZ = 3, offsetY = 1;

        for (int x = (int) (l.getX() - offsetX); x < l.getX() + offsetX; x++) {
            for (int z = (int) (l.getZ() - offsetZ); z < l.getZ() + offsetZ; z++) {
                for (int y = (int) (l.getY() - offsetY); y < l.getY() + offsetY; y++) {
                    Block test = w.getBlockAt(x, y, z);
                    if (!test.getChunk().isLoaded()) continue;

                    // TODO: Settings for place able
                    if (!test.getType().equals(Material.AIR)) continue;

                    acceptableBlocks.add(test);
                }
            }
        }

        // TODO: add chance to grow
        // Rand new spot
        int indexForNewSpot = random(0, acceptableBlocks.size());
        Block target = acceptableBlocks.get(indexForNewSpot);

        // TODO: Tiers of growing
        target.setType(Material.GRASS);

        MainPlugin.logger.info("X: " + target.getX() + " Z: " + target.getZ());
    }
}
