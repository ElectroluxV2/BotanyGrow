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
    public double minHumidityLvl = 0.0;
    public double maxHumidityLvl = 4.0;
    public boolean spread = false;
    public HashMap<Material, Integer> minNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> maxNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> minStrictNeighborhoods = new HashMap<>();
    public HashMap<Material, Integer> maxStrictNeighborhoods = new HashMap<>();
    public ArrayList<Biome> except = new ArrayList<>();
    public HashMap<Biome, Integer> exclusively = new HashMap<>();
    public Material material;
    public @Nullable BotanyTier previous = null;
    public HashMap<BotanyTier, Integer> next = new HashMap<>();

    public BotanyTier() {  }

    public ArrayList<BotanyTier> matchNext(Block target, NeighbourInfo neighbourInfo) {
        HashMap<BotanyTier, Integer> optionsLeft = new HashMap<>(next);

        ArrayList<BotanyTier> r = new ArrayList<>();
        for (Map.Entry<BotanyTier, Integer> entry : optionsLeft.entrySet()) {

            // Chance
            if (Math.round(Math.random()*100) <= entry.getValue()) {
                continue;
            }

            // Every option has to pass tests
            if (this.runTestOnBlock(target, entry.getKey(), neighbourInfo)) {
                r.add(entry.getKey());
            }
        }
        return r;
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
            if (tier.exclusively.get(target.getBiome()) == null) return false;

            // Chance
            if (Math.round(Math.random()*100) <= tier.exclusively.get(target.getBiome())) return false;
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
        return
        other.maxStrictNeighborhoods.equals(this.maxStrictNeighborhoods) &&
        other.minStrictNeighborhoods.equals(this.minStrictNeighborhoods) &&
        other.maxNeighborhoods.equals(this.maxNeighborhoods) &&
        other.minNeighborhoods.equals(this.minNeighborhoods) &&
        other.exclusively.equals(this.exclusively) &&
        other.except.equals(this.except) &&
        other.maxHumidityLvl == this.maxHumidityLvl &&
        other.minHumidityLvl == this.minHumidityLvl &&
        other.maxLightLvl == this.maxLightLvl &&
        other.minLightLvl == this.minLightLvl &&
        other.spread == this.spread &&
        other.material.equals(this.material);
    }

    @Override
    public int hashCode() {
        return minNeighborhoods == null ? 0 : minNeighborhoods.hashCode();
    }

    public BotanyTier deepCopy() {
        BotanyTier r = new BotanyTier();
        r.maxStrictNeighborhoods = this.maxStrictNeighborhoods;
        r.minStrictNeighborhoods = this.minStrictNeighborhoods;
        r.maxNeighborhoods = this.maxNeighborhoods;
        r.minNeighborhoods = this.minNeighborhoods;
        r.exclusively = this.exclusively;
        r.except = this.except;
        r.maxHumidityLvl = this.maxHumidityLvl;
        r.minHumidityLvl = this.minHumidityLvl;
        r.maxLightLvl = this.maxLightLvl;
        r.minLightLvl = this.minLightLvl;
        r.spread = this.spread;
        r.material = this.material;

        return r;
    }
}
