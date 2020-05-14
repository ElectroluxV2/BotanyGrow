package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class BlockFadeE implements Listener {

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent e) {
        if (!e.getBlock().getType().equals(Material.FARMLAND)) return;

        Block b = e.getBlock().getRelative(0, 1, 0);
        if (!Settings.crops.contains(b.getType())) return;
        b.breakNaturally();
    }
}
