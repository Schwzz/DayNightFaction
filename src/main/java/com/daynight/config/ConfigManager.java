package com.daynight.config;

import com.daynight.DayNightPlugin;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final DayNightPlugin plugin;

    @Getter private double damageBase;
    @Getter private double damageIncrement;
    @Getter private double damageMax;
    @Getter private int damageIntervalTicks;
    @Getter private double safeZoneRecovery;
    @Getter private int safeZoneRecoveryIntervalTicks;
    @Getter private int lightThreshold;

    @Getter private int effectsDelayTicks;
    @Getter private List<String> sunSeekerEffects;
    @Getter private List<String> nightStalkerEffects;
    @Getter private List<String> sunSeekerPassives;
    @Getter private List<String> nightStalkerPassives;

    @Getter private boolean disableBeds;
    @Getter private int gracePeriodSeconds;
    @Getter private String dangerSound;
    @Getter private String safetySound;
    @Getter private List<Integer> dangerEscalationSeconds;

    public ConfigManager(DayNightPlugin plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.load();
    }

    private void load() {
        this.plugin.reloadConfig();
        this.damageBase = this.plugin.getConfig().getDouble("damage.base", 1.0);
        this.damageIncrement = this.plugin.getConfig().getDouble("damage.increment", 0.5);
        this.damageMax = Math.max(this.damageBase, this.plugin.getConfig().getDouble("damage.max", 5.0));
        this.damageIntervalTicks = Math.max(20, this.plugin.getConfig().getInt("damage.interval-ticks", 100));
        this.safeZoneRecovery = Math.max(0.0, this.plugin.getConfig().getDouble("safe-zone-recovery.amount", 1.0));
        this.safeZoneRecoveryIntervalTicks = Math.max(20, this.plugin.getConfig().getInt("safe-zone-recovery.interval-ticks", 100));
        this.lightThreshold = this.plugin.getConfig().getInt("light-threshold", 8);
        this.effectsDelayTicks = Math.max(20, this.plugin.getConfig().getInt("effects.delay-ticks", 200));
        this.sunSeekerEffects = this.plugin.getConfig().getStringList("effects.sun-seeker");
        this.nightStalkerEffects = this.plugin.getConfig().getStringList("effects.night-stalker");
        this.sunSeekerPassives = this.plugin.getConfig().getStringList("passives.sun-seeker");
        this.nightStalkerPassives = this.plugin.getConfig().getStringList("passives.night-stalker");
        if (this.sunSeekerEffects.isEmpty()) {
            this.sunSeekerEffects = Arrays.asList("WEAKNESS:1", "SLOWNESS:1");
        }
        if (this.nightStalkerEffects.isEmpty()) {
            this.nightStalkerEffects = Arrays.asList("GLOWING:1", "WEAKNESS:1");
        }
        if (this.sunSeekerPassives.isEmpty()) {
            this.sunSeekerPassives = Arrays.asList("REGENERATION:1");
        }
        if (this.nightStalkerPassives.isEmpty()) {
            this.nightStalkerPassives = Arrays.asList("NIGHT_VISION:1", "SPEED:1");
        }
        this.disableBeds = this.plugin.getConfig().getBoolean("disable-beds", true);
        this.gracePeriodSeconds = this.plugin.getConfig().getInt("grace-period", 30);
        this.dangerSound = this.plugin.getConfig().getString("feedback.danger-sound", "ENTITY_ENDERMAN_STARE");
        this.safetySound = this.plugin.getConfig().getString("feedback.safety-sound", "ENTITY_PLAYER_LEVELUP");
        this.dangerEscalationSeconds = this.plugin.getConfig().getIntegerList("feedback.escalation-seconds");
        if (this.dangerEscalationSeconds.isEmpty()) {
            this.dangerEscalationSeconds = Arrays.asList(10, 30, 60);
        }
    }

    public void reload() {
        this.load();
    }
}