package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class ChunkUnloadE implements Listener {

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        ChunkInfo chunkInfo = new ChunkInfo(e.getChunk().getChunkSnapshot());
        MainPlugin.chunksToScan.remove(chunkInfo);
        MainPlugin.chunksScanned.remove(chunkInfo);
    }
}
