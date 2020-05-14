package io.github.electroluxv2.BotanyGrow;

import io.github.electroluxv2.BotanyGrow.commands.ReloadConfigs;
import io.github.electroluxv2.BotanyGrow.commands.TPS;
import io.github.electroluxv2.BotanyGrow.events.*;
import io.github.electroluxv2.BotanyGrow.logger.CustomLogger;
import io.github.electroluxv2.BotanyGrow.runable.Broadcast;
import io.github.electroluxv2.BotanyGrow.runable.ChunkScanner;
import io.github.electroluxv2.BotanyGrow.runable.FloraPopulate;
import io.github.electroluxv2.BotanyGrow.settings.FileManager;
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
import java.util.*;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public class MainPlugin extends JavaPlugin {

    private static MainPlugin instance;
    public static Logger logger;

    public final static HashMap<Integer, Integer> asyncMetrics = new HashMap<>();
    public final static ArrayList<BukkitTask> chunkScanners = new ArrayList<>();
    private final static ArrayList<Integer> scannersUsed = new ArrayList<>();
    private final static Queue<Integer> scannersUnused = new LinkedList<>();
    private BukkitTask floraPopulate;
    private BukkitTask broadcast;

    public final static HashMap<Integer, ArrayList<ChunkInfo>> chunksToScan = new HashMap<>();
    public final static HashMap<Integer, ArrayList<ChunkInfo>> chunksScanned = new HashMap<>();
    public final static ArrayList<Location> blocksToPopulate = new ArrayList<>();

    private boolean errorOnLoad = false;

    @Override
    public void onLoad() {
        // Instance
        instance = this;

        // Logger
        logger = new CustomLogger(instance);

        // Settings
        FileManager.checkFiles();
        if (Settings.load()) {
            logger.info("Loaded successful");
        } else {
            logger.log(SEVERE, "Can't load configs. Disabling");
            errorOnLoad = true;
        }
    }

    @Override
    public void onEnable() {

        if (errorOnLoad) {
            Bukkit.getPluginManager().disablePlugin(instance);
            return;
        }

        // Listeners
        Bukkit.getPluginManager().registerEvents(new ChunkLoadE(), instance);
        Bukkit.getPluginManager().registerEvents(new ChunkUnloadE(), instance);
        Bukkit.getPluginManager().registerEvents(new PlayerMoveE(), instance);
        Bukkit.getPluginManager().registerEvents(new BlockPhysicsE(), instance);
        Bukkit.getPluginManager().registerEvents(new BlockFadeE(), instance);


        // Commands
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            commandMap.register("TPS", new TPS());
            commandMap.register("BRL", new ReloadConfigs());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Init multithreading
        for (int i = 0; i < Settings.scannersCount; i++) {
            MainPlugin.logger.info(i + "c");
            BukkitTask scanner = new ChunkScanner().runTaskTimerAsynchronously(instance, 10, 1);
            chunkScanners.add(scanner);
            asyncMetrics.put(scanner.getTaskId(), 0);
            chunksToScan.put(scanner.getTaskId(), new ArrayList<>());
            chunksScanned.put(scanner.getTaskId(), new ArrayList<>());
            scannersUnused.add(scanner.getTaskId());
        }

        // Add loaded chunks
        for (World w : Bukkit.getWorlds()) {

            if (w.getName().contains("nether")) continue;
            if (w.getName().contains("the_end")) continue;

            // TODO disabled worlds
            for (Chunk c : w.getLoadedChunks()) {

                ChunkInfo chunkInfo = new ChunkInfo(c.getChunkSnapshot());
                chunksToScan.computeIfAbsent(getScannerId(), id -> new ArrayList<>()).add(chunkInfo);
            }
        }

        this.floraPopulate = new FloraPopulate().runTaskTimer(instance, 0, 1);
        //this.broadcast = new Broadcast().runTaskTimerAsynchronously(instance,0, 100);
        logger.info("Enabled successful");
    }

    public static int getScannerId() {
        int id = scannersUnused.remove();
        scannersUsed.add(id);

        if (scannersUnused.size() == 0) {
            scannersUnused.addAll(scannersUsed);
            scannersUsed.clear();
        }

        return id;
    }

    @Override
    public void onDisable() {
        if (!errorOnLoad) {
            for (BukkitTask task : chunkScanners) {
                if (!task.isCancelled()) task.cancel();
            }
            if (!this.broadcast.isCancelled()) this.broadcast.cancel();
            if (!this.floraPopulate.isCancelled()) this.floraPopulate.cancel();
        }

        logger.info("Disabled successful");
    }

    public static MainPlugin getInst() {
        return instance;
    }
}
