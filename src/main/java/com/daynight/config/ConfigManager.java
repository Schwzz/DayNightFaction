package com.daynight.config;

import com.daynight.DayNightPlugin;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final DayNightPlugin plugin;

    @Getter private double damageBase;
    @Getter private double damageIncrement;
    @Getter private int damageIntervalTicks;
    @Getter private int lightThreshold;

    @Getter private int effectsDelayTicks;
    @Getter private List<String> sunSeekerEffects;
    @Getter private List<String> nightStalkerEffects;

    public ConfigManager(DayNightPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.load();
    }

    private void load() {
        this.plugin.reloadConfig();
        this.damageBase = this.plugin.getConfig().getDouble("damage.base", 1.0);
        this.damageIncrement = this.plugin.getConfig().getDouble("damage.increment", 0.5);
        this.damageIntervalTicks = Math.max(20, this.plugin.getConfig().getInt("damage.interval-ticks", 100));
        this.lightThreshold = this.plugin.getConfig().getInt("light-threshold", 8);
        this.effectsDelayTicks = Math.max(20, this.plugin.getConfig().getInt("effects.delay-ticks", 200));
        this.sunSeekerEffects = this.plugin.getConfig().getStringList("effects.sun-seeker");
        this.nightStalkerEffects = this.plugin.getConfig().getStringList("effects.night-stalker");
        if (this.sunSeekerEffects.isEmpty()) {
            this.sunSeekerEffects = Arrays.asList("WEAKNESS:1", "SLOWNESS:1");
        }
        if (this.nightStalkerEffects.isEmpty()) {
            this.nightStalkerEffects = Arrays.asList("GLOWING:1", "WEAKNESS:1");
        }
    }

    public void reload() {
        this.load();
    }
}