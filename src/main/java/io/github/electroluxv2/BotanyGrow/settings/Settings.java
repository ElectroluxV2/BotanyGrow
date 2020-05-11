package io.github.electroluxv2.BotanyGrow.settings;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;

public class Settings {
    public HashMap<Material, ArrayList<Material>> placeAbleMaterials = new HashMap<>();

    public Settings() {
        ArrayList<Material> placeAbleForGrass = new ArrayList<>();
        placeAbleForGrass.add(Material.GRASS_BLOCK);
        placeAbleForGrass.add(Material.DIRT);
        placeAbleMaterials.put(Material.GRASS, placeAbleForGrass);
        placeAbleMaterials.put(Material.TALL_GRASS, placeAbleForGrass);
    }
}
