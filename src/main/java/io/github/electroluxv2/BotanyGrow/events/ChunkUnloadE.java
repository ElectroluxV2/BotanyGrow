package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitTask;

public class ChunkUnloadE implements Listener {

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        ChunkInfo chunkInfo = new ChunkInfo(e.getChunk().getChunkSnapshot());

        for (BukkitTask task : MainPlugin.chunkScanners) {
            int id = task.getTaskId();
            MainPlugin.chunksToScan.get(id).remove(chunkInfo);
            MainPlugin.chunksScanned.get(id).remove(chunkInfo);
        }
    }
}
