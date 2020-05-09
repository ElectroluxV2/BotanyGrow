package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkLoadE implements Listener {

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        ChunkSnapshot chunkSnapshot = e.getChunk().getChunkSnapshot();
        ChunkInfo c2 = new ChunkInfo(chunkSnapshot);

        if (MainPlugin.chunksScanned.contains(c2) && MainPlugin.chunksToScan.contains(c2)) {
            return;
        }

        if (c2.world.contains("nether")) return;
        if (c2.world.contains("the_end")) return;

        ChunkInfo chunkInfo = new ChunkInfo(e.getChunk().getChunkSnapshot());
        MainPlugin.chunksToScan.add(chunkInfo);
    }
}
