package io.github.electroluxv2.BotanyGrow.settings;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.BotanyTier;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;

public class Settings {
    public static HashMap<Material, HashMap<Material, Integer>> placeAbleMaterials = new HashMap<>();
    public static ArrayList<Material> materialsToScan = new ArrayList<>();
    public static HashMap<Material, BotanyTier> tiers = new HashMap<>();
    public static ArrayList<Material> multiBlocks = new ArrayList<>();
    public static ArrayList<Material> crops = new ArrayList<>();

    public static boolean load() {
        multiBlocks.add(Material.TALL_GRASS);
        multiBlocks.add(Material.TALL_SEAGRASS);
        multiBlocks.add(Material.LARGE_FERN);
        multiBlocks.add(Material.LILAC);
        multiBlocks.add(Material.ROSE_BUSH);
        multiBlocks.add(Material.SUNFLOWER);
        multiBlocks.add(Material.PEONY);

        crops.add(Material.WHEAT);
        crops.add(Material.BEETROOT);
        crops.add(Material.POTATO);
        crops.add(Material.PUMPKIN_STEM);
        crops.add(Material.MELON_STEM);
        crops.add(Material.CARROT);

        try {
            YamlConfiguration mainConfig = YamlConfiguration.loadConfiguration(FileManager.config);
            HashMap<String, BotanyTier> loadedTiers = loadTiers();
            loadConnections(loadedTiers);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    private static HashMap<String, BotanyTier> loadTiers() throws IOException {
        // TODO: Error handling
        HashMap<String, BotanyTier> r = new HashMap<>();
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(FileManager.tiers)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                LinkedHashMap<String, LinkedHashMap<String, Object>> map = (LinkedHashMap<String, LinkedHashMap<String, Object>>) o;
                Map.Entry<String, BotanyTier> e = parseTierYAML(map);
                assert e != null;
                r.put(e.getKey(), e.getValue());
            }

        } catch (IOException e) {
            MainPlugin.logger.log(Level.SEVERE, "Can't load tiers.yml");
            throw e;
        }

        return r;
    }

    @SuppressWarnings({"unchecked"})
    private static Map.Entry<String, BotanyTier> parseTierYAML(LinkedHashMap<String, LinkedHashMap<String, Object>> map) {
        // TODO: Error handling
        // TODO: Not obligatory options
        // TODO: Inheritance
        LinkedHashMap<String, Object> properties = map.get("properties");
        String name = properties.get("name").toString();
        Material material = Material.getMaterial(properties.get("material").toString());
        assert material != null;
        boolean spread = Boolean.parseBoolean(properties.get("spread").toString());

        LinkedHashMap<String, Object> requirements = map.get("requirements");
        HashMap<Material, Integer> canGrowOn = new HashMap<>();
        for (ArrayList<Object> list : ((List<ArrayList<Object>>) requirements.get("can-grow-on"))) {
            assert list.size() == 2;
            canGrowOn.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
        }

        if (!(requirements.get("light") instanceof LinkedHashMap)) return null;

        LinkedHashMap<String, Integer> light = (LinkedHashMap<String, Integer>) requirements.get("light");
        int minLight = light.get("min");
        int maxLight = light.get("max");

        LinkedHashMap<String, Double> humidity = (LinkedHashMap<String, Double>) requirements.get("humidity");
        double minHumidity = humidity.get("min");
        double maxHumidity = humidity.get("max");

        LinkedHashMap<String, Object> biomes = (LinkedHashMap<String, Object>) requirements.get("biomes");
        HashMap<Biome, Integer> exclusively = new HashMap<>();
        for (ArrayList<Object> list : ((List<ArrayList<Object>>) biomes.get("exclusively"))) {
            assert list.size() == 2;
            Biome biome = Biome.valueOf(list.get(0).toString());
            exclusively.put(biome, (int) list.get(1));
        }

        ArrayList<Biome> except = new ArrayList<>();
        for (String s : ((List<String>)biomes.get("except"))) {
            Biome biome = Biome.valueOf(s);
            except.add(biome);
        }

        LinkedHashMap<String, List<ArrayList<Object>>> neighborhoods = (LinkedHashMap<String, List<ArrayList<Object>>>) requirements.get("neighborhoods");
        HashMap<Material, Integer> maxNeighborhoods = new HashMap<>();
        for (ArrayList<Object> list : neighborhoods.get("max")) {
            assert list.size() == 2;
            maxNeighborhoods.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
        }

        HashMap<Material, Integer> minNeighborhoods = new HashMap<>();
        for (ArrayList<Object> list : neighborhoods.get("min")) {
            assert list.size() == 2;
            minNeighborhoods.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
        }

        LinkedHashMap<String, List<ArrayList<Object>>> neighborhoodsStrict = (LinkedHashMap<String, List<ArrayList<Object>>>) requirements.get("neighborhoods-strict");
        HashMap<Material, Integer> maxNeighborhoodsStrict = new HashMap<>();
        for (ArrayList<Object> list : neighborhoodsStrict.get("max")) {
            assert list.size() == 2;
            maxNeighborhoodsStrict.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
        }

        HashMap<Material, Integer> minNeighborhoodsStrict = new HashMap<>();
        for (ArrayList<Object> list : neighborhoodsStrict.get("max")) {
            assert list.size() == 2;
            minNeighborhoodsStrict.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
        }

        BotanyTier tier = new BotanyTier(material);
        tier.maxStrictNeighborhoods = maxNeighborhoodsStrict;
        tier.minStrictNeighborhoods = minNeighborhoodsStrict;
        tier.maxNeighborhoods = maxNeighborhoods;
        tier.minNeighborhoods = minNeighborhoods;
        tier.spread = spread;
        tier.maxLightLvl = maxLight;
        tier.minLightLvl = minLight;
        tier.maxHumidityLvl = maxHumidity;
        tier.minHumidityLvl = minHumidity;
        tier.except = except;
        tier.exclusively = exclusively;

        placeAbleMaterials.put(tier.material, canGrowOn);
        return new AbstractMap.SimpleEntry<>(name, tier);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void loadConnections(HashMap<String, BotanyTier> loadedTiers) {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(FileManager.connections)) {
            Iterable<Object> itr = yaml.loadAll(in);

            for (Object o : itr) {
                LinkedHashMap<String, Object> map = (LinkedHashMap) o;

                parseConnectionYAML(map, loadedTiers);
            }

        } catch (Exception e) {
            MainPlugin.logger.log(Level.SEVERE, "Can't load tiers.yml");
            e.printStackTrace();
        }
    }

    @SuppressWarnings({"unchecked"})
    private static void parseConnectionYAML(LinkedHashMap<String, Object> map, HashMap<String, BotanyTier> loadedTiers) {
        String tierName = (String) map.get("tier");
        List<ArrayList<Object>> nextTiers = (List<ArrayList<Object>>) map.getOrDefault("next", new ArrayList<>());
        String previous = (String) map.getOrDefault("previous", "");

        // Find source
        if (loadedTiers.get(tierName) == null) {
            MainPlugin.logger.warning("Unknown tier: + " + tierName);
            return;
        }
        BotanyTier source = loadedTiers.get(tierName);

        // Add previous
        if (!previous.isEmpty()) {
            BotanyTier previousTier = loadedTiers.get(previous);
            if (previousTier == null) {
                MainPlugin.logger.warning("Unknown tier: + " + tierName);
                return;
            }
            source.previous = previousTier;
        }

        // Add next
        for (ArrayList<Object> list : nextTiers) {
            assert list.size() == 2;
            BotanyTier next = loadedTiers.get(list.get(0).toString());

            if (next == null) {
                MainPlugin.logger.warning("Unknown tier: + " + tierName);
                continue;
            }

            source.next.put(next, (int) list.get(1));
        }

        materialsToScan.add(source.material);
        tiers.put(source.material, source);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<BotanyTier, Integer> entry : source.next.entrySet()) {
            sb.append(entry.getKey().material.name());
            sb.append(" ( ");
            sb.append(entry.getValue());
            sb.append("% ) | ");
        }

        String msg = source.material.name();
        if (source.next.size() > 0) {
            msg = msg + " => " + sb.toString().substring(0, sb.toString().length() - 3);
        }
        if (source.previous != null) msg = source.previous.material.name() + " <= " + msg;

        MainPlugin.logger.info("Link: " + msg);
    }
}
