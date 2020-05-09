package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksScanned;
import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksToScan;

public class ChunkScanner extends BukkitRunnable {

    private ChunkInfo currentChunkInfo = null;
    private int heightIndex = 0;
    public int splitParts = 1;

    private static int random(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public void run() {
        MainPlugin.logger.info("chunksToScan: " + chunksToScan.size() + " chunksScanned: " + chunksScanned.size());

        int randomIndex = 0;
        if (chunksToScan.size() != 0) {
            randomIndex = random(0, chunksToScan.size() - 1);
        }

        if (currentChunkInfo == null) {
            if (chunksToScan.size() == 0) {
                if (chunksScanned.size() == 0) return;

                chunksToScan.addAll(chunksScanned);
                chunksScanned.clear();
            }
            randomIndex = random(0, chunksToScan.size() - 1);
            currentChunkInfo = chunksToScan.get(randomIndex);
            if (currentChunkInfo == null) {
                chunksToScan.remove(randomIndex);
                return;
            }
        }

        // Get world
        World w = Bukkit.getWorld(currentChunkInfo.world);
        if (w == null) {
            chunksToScan.remove(currentChunkInfo);
            return;
        }

        // Get chunk
        ChunkSnapshot chunkSnapshot = w.getChunkAt(currentChunkInfo.x, currentChunkInfo.z).getChunkSnapshot();
        // If chunk is not loaded code at ChunkUnloadEvent should get rid of it
        // Scan whole chunk, block by block

        // Split into parts to gain some performance
        int currentMaxY = heightIndex == 0 ? 256 / splitParts : (256 / splitParts) * (heightIndex + 1);
        int currentStartY = heightIndex == 0 ? 0 : heightIndex * (256 / splitParts);

        for (int y = currentStartY; y < currentMaxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {

                    Location l = new Location(w, x, y, z);
                    // Only flora
                    Material m = chunkSnapshot.getBlockType(x, y, z);

                    // TODO settings for materials
                    if (m.equals(Material.GRASS)) {
                        // Add to synchronized thread
                        MainPlugin.logger.info("ADDED GRASS");
                        MainPlugin.blocksToPopulate.add(l);
                    }
                }
            }
        }

        heightIndex++;
        if (heightIndex == splitParts) {
            heightIndex = 0;
            chunksToScan.remove(randomIndex);
            chunksScanned.add(currentChunkInfo);
        }
    }
}
