package io.github.electroluxv2.BotanyGrow;

import io.github.electroluxv2.BotanyGrow.events.ChunkLoadE;
import io.github.electroluxv2.BotanyGrow.events.ChunkUnloadE;
import io.github.electroluxv2.BotanyGrow.logger.CustomLogger;
import io.github.electroluxv2.BotanyGrow.runable.ChunkScanner;
import io.github.electroluxv2.BotanyGrow.runable.FloraPopulate;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.logging.Logger;

public class MainPlugin extends JavaPlugin {

    // Instance
    private static MainPlugin instance;

    // Logger
    public static Logger logger;

    private BukkitTask chunkScanner;
    private BukkitTask floraPopulate;

    public static ArrayList<ChunkInfo> chunksToScan = new ArrayList<ChunkInfo>();
    public static ArrayList<ChunkInfo> chunksScanned = new ArrayList<ChunkInfo>();
    public static ArrayList<Location> blocksToPopulate = new ArrayList<Location>();

    @Override
    public void onEnable() {

        // Instance
        instance = this;

        // Logger
        logger = new CustomLogger(instance);

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ChunkLoadE(), instance);
        Bukkit.getPluginManager().registerEvents(new ChunkUnloadE(), instance);

        // Add loaded chunks
        for (World w : Bukkit.getWorlds()) {

            if (w.getName().contains("nether")) return;
            if (w.getName().contains("the_end")) return;

            // TODO disabled worlds
            for (Chunk c : w.getLoadedChunks()) {

                ChunkInfo chunkInfo = new ChunkInfo(c.getChunkSnapshot());
                chunksToScan.add(chunkInfo);
            }
        }

        this.chunkScanner = new ChunkScanner().runTaskTimerAsynchronously(instance, 0, 1);
        this.floraPopulate = new FloraPopulate().runTaskTimer(instance, 0, 1);
        logger.info("Loaded successful");
    }

    @Override
    public void onDisable() {
        this.chunkScanner.cancel();
        this.floraPopulate.cancel();
        logger.info("Disabled successful");
    }

    public static MainPlugin getInst() {
        return instance;
    }
}
