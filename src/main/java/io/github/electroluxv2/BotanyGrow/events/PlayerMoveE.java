package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import static java.lang.Math.abs;

public class PlayerMoveE implements Listener {

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Chunk c = e.getFrom().getChunk();
        ChunkInfo ci = new ChunkInfo(c.getChunkSnapshot());

        //if (MainPlugin.chunksToScan.contains(ci)) return;
        //MainPlugin.chunksToScan.add(ci);
    }
}
