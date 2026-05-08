package com.daynight.listener;

import com.daynight.DayNightPlugin;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BedListener implements Listener {
    private final DayNightPlugin plugin;

    public BedListener(DayNightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (!this.plugin.getConfigManager().isDisableBeds()) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot escape the cycle by sleeping.");
    }
}