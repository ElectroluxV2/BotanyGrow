package io.github.electroluxv2.BotanyGrow.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;

public class NeighbourInfo {

    public ArrayList<Block> neighbours = new ArrayList<>();
    public HashMap<Material, Integer> neighboursSums = new HashMap<>();
    public ArrayList<Block> strictNeighbours = new ArrayList<>();
    public HashMap<Material, Integer> strictNeighboursSums = new HashMap<>();

    public NeighbourInfo(Block target) {

        // Count neighbours
        Location l = target.getLocation();
        World w = target.getWorld();
        for (int x = l.getBlockX() - 1; x <= l.getBlockX() + 1; x++) {
            for (int z = l.getBlockZ() - 1; z <= l.getBlockZ() + 1; z++) {
                for (int y = l.getBlockY() - 1; y <= l.getBlockY() + 1; y++) {
                    // Chunk have to be loaded in order to modify
                    Location n = new Location(w, x, y, z);
                    if (!w.getChunkAt(l).isLoaded()) continue;

                    Block b = w.getBlockAt(n);
                    Material m = b.getType();

                    neighbours.add(b);
                    neighboursSums.merge(m,1, Integer::sum);

                    if (b.getLocation().getBlockY() != l.getBlockY()) continue;
                    if (Math.abs(b.getLocation().getBlockZ() - l.getBlockZ()) > 1) continue;
                    if (Math.abs(b.getLocation().getBlockX() - l.getBlockX()) > 1) continue;
                    if (b.getLocation().getBlock().equals(target)) continue;

                    strictNeighbours.add(b);
                    strictNeighboursSums.merge(m, 1, Integer::sum);
                }
            }
        }
    }
}
