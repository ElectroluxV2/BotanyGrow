package io.github.electroluxv2.BotanyGrow.runable;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Broadcast extends BukkitRunnable {

    private static class Info {
        public int scanned;
        public int toScan;
    }

    @Override
    public void run() {

        HashMap<Integer, Info> stats = new HashMap<>();
        for (BukkitTask entry : MainPlugin.chunkScanners) {
            int id = entry.getTaskId();

            Info i = new Info();

            i.scanned = MainPlugin.chunksScanned.get(id).size();
            i.toScan = MainPlugin.chunksToScan.get(id).size();

            stats.putIfAbsent(id, i);
        }

        StringBuilder sb = new StringBuilder();

        for (Entry<Integer, Info> entry : stats.entrySet()) {
            int id = entry.getKey();
            Info i = entry.getValue();

            sb.append("S#");
            sb.append(id);
            sb.append("[");
            sb.append(i.scanned);
            sb.append(" / ");
            sb.append(i.toScan);
            sb.append("] ");
        }

        String msg = sb.toString();
        if (msg.length() > 2) msg = msg.substring(0, msg.length()-1);
        MainPlugin.logger.info(msg);
        //Bukkit.getServer().broadcastMessage("Actions done: " + msg);
    }
}
