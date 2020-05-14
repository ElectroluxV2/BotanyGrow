package io.github.electroluxv2.BotanyGrow.settings;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.utils.BotanyTier;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

public class Settings {
    public static HashMap<Material, HashMap<Material, Integer>> placeAbleMaterials = new HashMap<>();
    public static ArrayList<Material> materialsToScan = new ArrayList<>();
    public static HashMap<Material, BotanyTier> tiers = new HashMap<>();
    public static ArrayList<Material> multiBlocks = new ArrayList<>();
    public static ArrayList<Material> crops = new ArrayList<>();
    public static int scannersCount = 2;
    public static int chunkParts = 1;

    public static boolean load() {
        multiBlocks.add(Material.TALL_GRASS);
        multiBlocks.add(Material.TALL_SEAGRASS);
        multiBlocks.add(Material.LARGE_FERN);
        multiBlocks.add(Material.LILAC);
        multiBlocks.add(Material.ROSE_BUSH);
        multiBlocks.add(Material.SUNFLOWER);
        multiBlocks.add(Material.PEONY);

        crops.add(Material.WHEAT);
        crops.add(Material.BEETROOTS);
        crops.add(Material.POTATOES);
        crops.add(Material.PUMPKIN_STEM);
        crops.add(Material.MELON_STEM);
        crops.add(Material.CARROTS);

        try {
            YamlConfiguration mainConfig = YamlConfiguration.loadConfiguration(FileManager.config);
            scannersCount = mainConfig.getInt("chunk-scanners");
            chunkParts = mainConfig.getInt("chunk-parts");
            HashMap<String, BotanyTier> loadedTiers = loadTiers();
            loadConnections(loadedTiers);
        } catch (Exception e) {
            e.printStackTrace();
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
                Map.Entry<String, BotanyTier> e = parseTierYAML(map, r);
                if (e == null) continue;
                r.put(e.getKey(), e.getValue());
            }

        } catch (IOException e) {
            MainPlugin.logger.log(Level.SEVERE, "Can't load tiers.yml");
            throw e;
        }

        return r;
    }

    @SuppressWarnings({"unchecked"})
    private static Map.Entry<String, BotanyTier> parseTierYAML(LinkedHashMap<String, LinkedHashMap<String, Object>> map, HashMap<String, BotanyTier> alreadyLoaded) {
        // TODO: Error handling
        LinkedHashMap<String, Object> properties = map.get("properties");
        String name = properties.get("name").toString();

        BotanyTier tier;
        if (properties.get("inherits") != null) {
            if (alreadyLoaded.get(properties.get("inherits").toString()) == null) {
                MainPlugin.logger.warning("Unknown tier: + " + properties.get("inherits").toString());
                return null;
            }
            tier = alreadyLoaded.get(properties.get("inherits").toString()).deepCopy();
        } else {
            tier = new BotanyTier();
        }

        if  (properties.get("material") != null) {
            tier.material = Material.getMaterial(properties.get("material").toString());
        }

        if (properties.get("spread") != null) {
            tier.spread = Boolean.parseBoolean(properties.get("spread").toString());
        }

        if (map.get("requirements") != null) {
            LinkedHashMap<String, Object> requirements = map.get("requirements");

            if (requirements.get("can-grow-on") != null) {
                HashMap<Material, Integer> canGrowOn = new HashMap<>();
                for (ArrayList<Object> list : ((List<ArrayList<Object>>) requirements.get("can-grow-on"))) {
                    assert list.size() == 2;
                    canGrowOn.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
                }
                placeAbleMaterials.put(tier.material, canGrowOn);
            } else if (properties.get("inherits") != null) {
                placeAbleMaterials.put(tier.material, placeAbleMaterials.get(tier.material));
            } else {
                MainPlugin.logger.warning("Missing 'can-grow-on' for: " + name);
            }

            if (requirements.get("light") != null) {
                LinkedHashMap<String, Integer> light = (LinkedHashMap<String, Integer>) requirements.get("light");
                if (light.get("min") != null)
                    tier.minLightLvl = light.get("min");
                if (light.get("max") != null)
                    tier.maxLightLvl = light.get("max");
            }

            if (requirements.get("humidity") != null) {
                LinkedHashMap<String, Double> humidity = (LinkedHashMap<String, Double>) requirements.get("humidity");
                if (humidity.get("min") != null)
                    tier.minHumidityLvl = humidity.get("min");
                if (humidity.get("max") != null)
                    tier.maxHumidityLvl = humidity.get("max");
            }

            HashMap<Biome, Integer> exclusively = new HashMap<>();
            ArrayList<Biome> except = new ArrayList<>();
            if (requirements.get("biomes") != null) {
                LinkedHashMap<String, Object> biomes = (LinkedHashMap<String, Object>) requirements.get("biomes");

                if (biomes.get("exclusively") != null) {
                    for (ArrayList<Object> list : ((List<ArrayList<Object>>) biomes.get("exclusively"))) {
                        assert list.size() == 2;
                        Biome biome = Biome.valueOf(list.get(0).toString());
                        exclusively.put(biome, (int) list.get(1));
                    }
                }

                if (biomes.get("except") != null) {
                    for (String s : ((List<String>) biomes.get("except"))) {
                        Biome biome = Biome.valueOf(s);
                        except.add(biome);
                    }
                }
            }

            if (except.size() > 0) {
                tier.except = except;
            }

            if (exclusively.size() > 0) {
                tier.exclusively = exclusively;
            }

            HashMap<Material, Integer> maxNeighborhoods = new HashMap<>();
            HashMap<Material, Integer> minNeighborhoods = new HashMap<>();
            if (requirements.get("neighborhoods") != null) {
                LinkedHashMap<String, List<ArrayList<Object>>> neighborhoods = (LinkedHashMap<String, List<ArrayList<Object>>>) requirements.get("neighborhoods");

                if (neighborhoods.get("max") != null) {
                    for (ArrayList<Object> list : neighborhoods.get("max")) {
                        assert list.size() == 2;
                        maxNeighborhoods.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
                    }
                }

                if (neighborhoods.get("min") != null) {
                    for (ArrayList<Object> list : neighborhoods.get("min")) {
                        assert list.size() == 2;
                        minNeighborhoods.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
                    }
                }
            }

            if (maxNeighborhoods.size() > 0) {
                tier.maxNeighborhoods = maxNeighborhoods;
            }

            if (minNeighborhoods.size() > 0) {
                tier.minNeighborhoods = minNeighborhoods;
            }

            HashMap<Material, Integer> maxNeighborhoodsStrict = new HashMap<>();
            HashMap<Material, Integer> minNeighborhoodsStrict = new HashMap<>();
            if (requirements.get("neighborhoods-strict") != null) {
                LinkedHashMap<String, List<ArrayList<Object>>> neighborhoodsStrict = (LinkedHashMap<String, List<ArrayList<Object>>>) requirements.get("neighborhoods-strict");

                if (neighborhoodsStrict.get("max") != null) {
                    for (ArrayList<Object> list : neighborhoodsStrict.get("max")) {
                        assert list.size() == 2;
                        maxNeighborhoodsStrict.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
                    }
                }

                if (neighborhoodsStrict.get("max") != null) {
                    for (ArrayList<Object> list : neighborhoodsStrict.get("max")) {
                        assert list.size() == 2;
                        minNeighborhoodsStrict.put(Material.getMaterial(list.get(0).toString()), (int) list.get(1));
                    }
                }
            }

            if (maxNeighborhoodsStrict.size() > 0) {
                tier.maxStrictNeighborhoods = maxNeighborhoodsStrict;
            }

            if (minNeighborhoodsStrict.size() > 0) {
                tier.minStrictNeighborhoods = minNeighborhoodsStrict;
            }

        }

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
                MainPlugin.logger.warning("Unknown tier: + " + previous);
                return;
            }
            source.previous = previousTier;
        }

        // Add next
        for (ArrayList<Object> list : nextTiers) {
            assert list.size() == 2;
            BotanyTier next = loadedTiers.get(list.get(0).toString());

            if (next == null) {
                MainPlugin.logger.warning("Unknown tier: + " + list.get(0).toString());
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
