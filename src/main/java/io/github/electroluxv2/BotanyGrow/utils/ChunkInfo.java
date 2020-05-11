package io.github.electroluxv2.BotanyGrow.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;

import static java.lang.Math.abs;

public class ChunkInfo {
    public int x;
    public int z;
    public String world;

    public ChunkInfo(ChunkSnapshot chunkSnapshot) {
        x = chunkSnapshot.getX();
        z = chunkSnapshot.getZ();
        world = chunkSnapshot.getWorldName();
    }

    public ChunkInfo(int x, int z, String world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public Location getWorldLocationFromInChunkLocation(int xInChunk, int y, int zInChunk) {
        Location r = new Location(Bukkit.getWorld(world), x * 16, y, z * 16);
        r.add(xInChunk,0,0);
        r.add(0,0, zInChunk);
        return r;
    }

    public Location getInChunkLocationFromWorldLocation(int xWorld, int y, int zWorld) {
        int xInChunk = abs(xWorld - (x * 16));
        int zInChunk = abs(zWorld - (z * 16));

        return new Location(Bukkit.getWorld(world), xInChunk, y, zInChunk);
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
