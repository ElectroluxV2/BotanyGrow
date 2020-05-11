package io.github.electroluxv2.BotanyGrow;

import io.github.electroluxv2.BotanyGrow.commands.TPS;
import io.github.electroluxv2.BotanyGrow.events.ChunkLoadE;
import io.github.electroluxv2.BotanyGrow.events.ChunkUnloadE;
import io.github.electroluxv2.BotanyGrow.events.PlayerMoveE;
import io.github.electroluxv2.BotanyGrow.logger.CustomLogger;
import io.github.electroluxv2.BotanyGrow.runable.ChunkScanner;
import io.github.electroluxv2.BotanyGrow.runable.FloraPopulate;
import io.github.electroluxv2.BotanyGrow.settings.Settings;
import io.github.electroluxv2.BotanyGrow.utils.ChunkInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;

public class MainPlugin extends JavaPlugin {

    // Instance
    private static MainPlugin instance;

    // Logger
    public static Logger logger;

    // Settings
    public static Settings settings;

    private BukkitTask chunkScanner;
    private BukkitTask floraPopulate;

    public static ArrayList<ChunkInfo> chunksToScan = new ArrayList<>();
    public static ArrayList<ChunkInfo> chunksScanned = new ArrayList<>();
    public static ArrayList<Location> blocksToPopulate = new ArrayList<>();

    @Override
    public void onEnable() {


        // Instance
        instance = this;

        // Logger
        logger = new CustomLogger(instance);

        // Settings
        settings = new Settings();

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ChunkLoadE(), instance);
        Bukkit.getPluginManager().registerEvents(new ChunkUnloadE(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveE(), instance);

        // Commands
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register("TPS", new TPS());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add loaded chunks
        for (World w : Bukkit.getWorlds()) {

            if (w.getName().contains("nether")) continue;
            if (w.getName().contains("the_end")) continue;

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
        if (!this.chunkScanner.isCancelled()) this.chunkScanner.cancel();
        if (!this.floraPopulate.isCancelled()) this.floraPopulate.cancel();
        logger.info("Disabled successful");
    }

    public static MainPlugin getInst() {
        return instance;
    }
}
