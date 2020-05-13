package io.github.electroluxv2.BotanyGrow.utils;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BotanyTier {
    public int minLightLvl = 0;
    public int maxLightLvl = 15;
    public double minHumidityLvl = 0;
    public double maxHumidityLvl = 100;
    public boolean spread = false;
    public HashMap<Material, Integer> minNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> maxNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> minStrictNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> maxStrictNeighborhoods = new HashMap<>();
    public ArrayList<Biome> except = new ArrayList<>();
    public ArrayList<Biome> exclusively = new ArrayList<>();
    public Material material;
    public @Nullable BotanyTier previous = null;
    public ArrayList<BotanyTier> next = new ArrayList<>();

    public BotanyTier(Material m, int l) {
        this.material = m;
        this.minLightLvl = l;
    }

    public BotanyTier(Material m) {
        this.material = m;
    }

    public ArrayList<BotanyTier> matchNext(Block target, NeighbourInfo neighbourInfo) {
        ArrayList<BotanyTier> optionsLeft = new ArrayList<>(next);

        // Every option has to pass tests
        optionsLeft.removeIf(option -> !this.runTestOnBlock(target, option, neighbourInfo));

        return optionsLeft;
    }

    public boolean canSpreadOn(Block target, NeighbourInfo neighbourInfo) {
        return this.runTestOnBlock(target, this, neighbourInfo);
    }

    private boolean runTestOnBlock(Block target, BotanyTier tier, NeighbourInfo neighbourInfo) {
        // Light level
        if (target.getLightFromSky() < tier.minLightLvl || target.getLightFromSky() > tier.maxLightLvl) {
            return false;
        }

        // Humidity level
        if (target.getHumidity() < tier.minHumidityLvl || target.getHumidity() > tier.maxHumidityLvl) {
            return false;
        }

        // Biome tests
        if (tier.except.contains(target.getBiome())) return false;
        if (tier.exclusively.size() > 0) {
            if (!tier.exclusively.contains(target.getBiome())) return false;
        }

        // Max neighbour
        boolean maxNeighborhoodsTest = true;
        for (Map.Entry<Material, Integer> entry : tier.maxNeighborhoods.entrySet()) {
            Material k = entry.getKey();
            Integer v = entry.getValue();

            if (neighbourInfo.neighboursSums.getOrDefault(k, 0) > v) {
                // This option is invalid for provided spot
                maxNeighborhoodsTest = false;
                break;
            }
        }

        if (!maxNeighborhoodsTest) return false;

        // Maximum strict neighborhoods
        boolean maxStrictNeighborhoodsTest = true;


        for (Map.Entry<Material, Integer> entry : tier.maxStrictNeighborhoods.entrySet()) {
            Material k = entry.getKey();
            Integer v = entry.getValue();

            if (neighbourInfo.strictNeighboursSums.getOrDefault(k, 0) > v) {
                // This option is invalid for provided spot
                maxStrictNeighborhoodsTest = false;
                break;
            }
        }

        if (!maxStrictNeighborhoodsTest) return false;

        // Minimum strict neighborhoods
        boolean minStrictNeighborhoodsTest = true;


        for (Map.Entry<Material, Integer> entry : tier.minStrictNeighborhoods.entrySet()) {
            Material k = entry.getKey();
            Integer v = entry.getValue();

            if (neighbourInfo.strictNeighboursSums.getOrDefault(k, 0) < v) {
                // This option is invalid for provided spot
                minStrictNeighborhoodsTest = false;
                break;
            }
        }

        if (!minStrictNeighborhoodsTest) return false;

        // Min neighbour
        for (Map.Entry<Material, Integer> entry : tier.minNeighborhoods.entrySet()) {
            Material k = entry.getKey();
            Integer v = entry.getValue();

            if (neighbourInfo.neighboursSums.getOrDefault(k, 0) < v) {
                // This option is invalid for provided spot
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof BotanyTier))
            return false;
        BotanyTier other = (BotanyTier) obj;
        return minNeighborhoods.equals(other.minNeighborhoods) && maxNeighborhoods.equals(other.maxNeighborhoods) && minHumidityLvl == other.minHumidityLvl && minLightLvl == other.minLightLvl;
    }

    @Override
    public int hashCode() {
        return minNeighborhoods == null ? 0 : minNeighborhoods.hashCode();
    }
}
