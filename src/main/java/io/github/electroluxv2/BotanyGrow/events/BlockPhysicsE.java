package io.github.electroluxv2.BotanyGrow.events;

import io.github.electroluxv2.BotanyGrow.settings.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockPhysicsE implements Listener {

    @EventHandler
    public void onBlockPhysicsEvent(BlockPhysicsEvent e) {
        if (!Settings.crops.contains(e.getSourceBlock().getType())) return;
        e.setCancelled(true);
    }
}
