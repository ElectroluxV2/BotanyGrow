package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.settings.Settings;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.scheduler.BukkitRunnable;
import sun.applet.Main;

import java.util.ArrayList;
import java.util.Random;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksScanned;
import static io.github.electroluxv2.BotanyGrow.MainPlugin.chunksToScan;

public class ChunkScanner extends BukkitRunnable {

    private ChunkInfo currentChunkInfo = null;
    private int heightIndex = 0;
    public int splitParts = 1;

    private static int randomIndex(int size) {
        Random r = new Random();
        return r.nextInt(size);
    }

    public void run() {
        //MainPlugin.logger.info("chunksToScan: " + chunksToScan.size() + " chunksScanned: " + chunksScanned.size());
        if (currentChunkInfo == null) {
            if (chunksToScan.size() == 0) {
                if (chunksScanned.size() == 0) return;

                chunksToScan.addAll(chunksScanned);
                chunksScanned.clear();
            }
            int randomIndex = randomIndex(chunksToScan.size());
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
        ChunkSnapshot chunkSnapshot;
        try {
            chunkSnapshot = w.getChunkAt(currentChunkInfo.x, currentChunkInfo.z).getChunkSnapshot();
        } catch (Exception e) {
            // Chunk could be in use
            return;
        }

        // If chunk is not loaded code at ChunkUnloadEvent should get rid of it


        // Split into parts to gain some performance
        int currentMaxY = heightIndex == 0 ? 256 / splitParts : (256 / splitParts) * (heightIndex + 1);
        int currentStartY = heightIndex == 0 ? 0 : heightIndex * (256 / splitParts);

        // Scan whole chunk, block by block
        for (int y = currentStartY; y < currentMaxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    // TODO: Works good in synchronized task but sometimes become null in async
                    if (currentChunkInfo == null) continue;

                    // Transform to world location insteadof in chunk location
                    Location l = currentChunkInfo.getWorldLocationFromInChunkLocation(x, y, z);
                    // Only flora
                    Material m = chunkSnapshot.getBlockType(x, y, z);

                    ArrayList<Material> toScan = Settings.materialsToScan;
                    if (toScan.contains(m)) {

                        // Skip top block of multi blocks
                        if (Settings.multiBlocks.contains(m)) {
                            Bisected data = (Bisected) chunkSnapshot.getBlockData(x, y, z);
                            if (data.getHalf().equals(Bisected.Half.TOP)) continue;
                        }

                        // Do not double if synchronized task is behind async task
                        if (MainPlugin.blocksToPopulate.contains(l)) continue;

                        // Add to synchronized thread
                        MainPlugin.blocksToPopulate.add(l);
                    }
                }
            }
        }

        heightIndex++;
        if (heightIndex == splitParts) {
            heightIndex = 0;

            chunksToScan.remove(currentChunkInfo);
            chunksScanned.add(currentChunkInfo);
            currentChunkInfo = null;
        }
    }
}
