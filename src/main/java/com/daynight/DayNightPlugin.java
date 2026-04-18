package com.daynight;

import com.daynight.command.AdminCommand;
import com.daynight.config.ConfigManager;
import com.daynight.faction.FactionManager;
import com.daynight.inventory.gui.GUIListener;
import com.daynight.inventory.gui.GUIManager;
import com.daynight.listener.JoinListener;
import com.daynight.task.GameTask;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class DayNightPlugin extends JavaPlugin {
    @Getter private ConfigManager configManager;
    @Getter private FactionManager factionManager;
    @Getter private GUIManager guiManager;
    @Getter private GameTask gameTask;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.factionManager = new FactionManager(this);
        this.guiManager = new GUIManager();

        this.getServer().getPluginManager().registerEvents(new GUIListener(this.guiManager), this);
        this.getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        AdminCommand adminCommand = new AdminCommand(this);
        this.getCommand("dnf").setExecutor(adminCommand);
        this.getCommand("dnf").setTabCompleter(adminCommand);

        this.gameTask = new GameTask(this);
        this.gameTask.runTaskTimer(this, 20L, 20L);
    }

    @Override
    public void onDisable() {
        this.factionManager.saveData();
    }
}