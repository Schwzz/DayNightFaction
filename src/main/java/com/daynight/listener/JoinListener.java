package com.daynight.listener;

import com.daynight.DayNightPlugin;
import com.daynight.inventory.impl.ChoiceGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
    private final DayNightPlugin plugin;

    public JoinListener(DayNightPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!this.plugin.getFactionManager().hasFaction(event.getPlayer().getUniqueId())) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                ChoiceGUI gui = new ChoiceGUI(this.plugin);
                this.plugin.getGuiManager().openGUI(gui, event.getPlayer());
            }, 20L);
        }
    }
}