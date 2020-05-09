package io.github.electroluxv2.BotanyGrow.utils;

import org.bukkit.ChunkSnapshot;

public class ChunkInfo {
    public int x;
    public int z;
    public String world;

    public ChunkInfo(ChunkSnapshot chunkSnapshot) {
        x = chunkSnapshot.getX();
        z = chunkSnapshot.getZ();
        world = chunkSnapshot.getWorldName();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof ChunkInfo))
            return false;
        ChunkInfo other = (ChunkInfo) obj;
        return world.equals(other.world) && x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        return world == null ? 0 : world.hashCode();
    }
}
