package io.github.electroluxv2.BotanyGrow.commands;

import io.github.electroluxv2.BotanyGrow.MainPlugin;
import io.github.electroluxv2.BotanyGrow.settings.Settings;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ReloadConfigs extends BukkitCommand {

    public ReloadConfigs() {
        super("brl", "Reloads BotanyGrow configs", "/brl", new ArrayList<>());
        this.setPermission("botanygrow.brl");
    }

    @Override
    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {

        Settings.placeAbleMaterials.clear();
        Settings.materialsToScan.clear();
        Settings.tiers.clear();
        Settings.multiBlocks.clear();

        Settings.load();

        if (commandSender instanceof Player) commandSender.sendMessage(ChatColor.GREEN + "Reloading BotanyGrow configs finished without warnings.");
        MainPlugin.logger.info("Reloading BotanyGrow configs finished without warnings.");

        return true;
    }
}
