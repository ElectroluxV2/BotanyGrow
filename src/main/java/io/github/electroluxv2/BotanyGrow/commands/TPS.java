package io.github.electroluxv2.BotanyGrow.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;

public class TPS extends BukkitCommand {
    private final String name = Bukkit.getServer().getClass().getPackage().getName();
    private final String version = name.substring(name.lastIndexOf('.') + 1);
    private final DecimalFormat format = new DecimalFormat("##.##");

    public TPS() {
        super("tps");
        this.description = "Returns current server health.";
        this.usageMessage = "/tps";
        this.setPermission("botanygrow.tps");
    }

    private Class<?> getNMSClass() {
        try {
            return Class.forName("net.minecraft.server." + version + "." + "MinecraftServer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {

        try {
            Object serverInstance = getNMSClass().getMethod("getServer").invoke(null);
            Field tpsField = serverInstance.getClass().getField("recentTps");
            double[] tps = ((double[]) tpsField.get(serverInstance));
            String msg = ChatColor.GOLD + "TPS from last 1m, 5m, 15m: " + tps[0] + " " + tps[1] + " " + tps[2] + ".";
            commandSender.sendMessage(msg);

        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
