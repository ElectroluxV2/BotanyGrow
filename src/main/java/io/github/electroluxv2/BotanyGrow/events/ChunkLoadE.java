package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.*;

public class ChunkLoadE implements Listener {

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        ChunkSnapshot chunkSnapshot = e.getChunk().getChunkSnapshot();
        ChunkInfo chunkInfo = new ChunkInfo(chunkSnapshot);

        int taskId = MainPlugin.getScannerId();

        if (MainPlugin.chunksScanned.get(taskId).contains(chunkInfo) && chunksToScan.get(taskId).contains(chunkInfo)) {
            return;
        }

        if (chunkInfo.world.contains("nether")) return;
        if (chunkInfo.world.contains("the_end")) return;

        chunksToScan.computeIfAbsent(taskId, id -> new ArrayList<>()).add(chunkInfo);

        //MainPlugin.logger.info("Added chunk to " + taskId);
    }
}
