package io.github.electroluxv2.BotanyGrow.settings;

import java.io.File;

import static io.github.electroluxv2.BotanyGrow.MainPlugin.getInst;

public class FileManager {
    static File config;
    static File tiers;
    static File connections;

    public static void checkFiles() {

        // Main folder
        if (!getInst().getDataFolder().exists()) {
            getInst().getDataFolder().mkdir();
        }

        // Config file
        config = new File(getInst().getDataFolder(), "config.yml");
        tiers = new File(getInst().getDataFolder(), "tiers.yml");
        connections = new File(getInst().getDataFolder(), "connections.yml");

        if (!config.exists()){
            getInst().saveDefaultConfig();
        }

        if (!tiers.exists()){
            getInst().saveResource("tiers.yml", true);
        }

        if (!connections.exists()){
            getInst().saveResource("connections.yml", true);
        }
    }
}
