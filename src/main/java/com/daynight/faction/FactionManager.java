package com.daynight.faction;

import com.daynight.DayNightPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FactionManager {
    private final DayNightPlugin plugin;
    private final Map<UUID, Faction> factionMap = new HashMap<>();
    private final Map<UUID, Long> exemptedPlayers = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public FactionManager(DayNightPlugin plugin) {
        this.plugin = plugin;
        this.loadData();
    }

    private void loadData() {
        this.dataFile = new File(this.plugin.getDataFolder(), "factions.yml");
        if (!this.dataFile.exists()) {
            this.dataFile.getParentFile().mkdirs();
            try {
                this.dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(this.dataFile);
        if (this.dataConfig.contains("factions")) {
            for (String key : this.dataConfig.getConfigurationSection("factions").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    String factionName = this.dataConfig.getString("factions." + key);
                    Faction faction = Faction.valueOf(factionName);
                    this.factionMap.put(uuid, faction);
                } catch (Exception ignored) {
                }
            }
        }
        if (this.dataConfig.contains("exempted")) {
            if (this.dataConfig.isList("exempted")) {
                for (String key : this.dataConfig.getStringList("exempted")) {
                    try {
                        this.exemptedPlayers.put(UUID.fromString(key), 0L);
                    } catch (Exception ignored) {
                    }
                }
            } else if (this.dataConfig.isConfigurationSection("exempted")) {
                for (String key : this.dataConfig.getConfigurationSection("exempted").getKeys(false)) {
                    try {
                        this.exemptedPlayers.put(UUID.fromString(key), this.dataConfig.getLong("exempted." + key, 0L));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public void saveData() {
        this.factionMap.forEach((uuid, faction) ->
                this.dataConfig.set("factions." + uuid.toString(), faction.name())
        );
        this.dataConfig.set("exempted", null);
        this.exemptedPlayers.forEach((uuid, expiresAt) ->
                this.dataConfig.set("exempted." + uuid.toString(), expiresAt)
        );
        try {
            this.dataConfig.save(this.dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasFaction(UUID uuid) {
        return this.factionMap.containsKey(uuid);
    }

    public Faction getFaction(UUID uuid) {
        return this.factionMap.get(uuid);
    }

    public void setFaction(UUID uuid, Faction faction) {
        this.factionMap.put(uuid, faction);
        this.saveData();
    }

    public void removeFaction(UUID uuid) {
        this.factionMap.remove(uuid);
        this.dataConfig.set("factions." + uuid.toString(), null);
        this.saveData();
    }

    public void exempt(UUID uuid) {
        exempt(uuid, 0L);
    }

    public void exempt(UUID uuid, long durationMillis) {
        this.exemptedPlayers.put(uuid, durationMillis > 0L ? System.currentTimeMillis() + durationMillis : 0L);
        this.saveData();
    }

    public void unexempt(UUID uuid) {
        this.exemptedPlayers.remove(uuid);
        this.saveData();
    }

    public boolean isExempted(UUID uuid) {
        Long expiresAt = this.exemptedPlayers.get(uuid);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt > 0L && expiresAt <= System.currentTimeMillis()) {
            this.exemptedPlayers.remove(uuid);
            this.saveData();
            return false;
        }
        return true;
    }

    public long getExemptionRemainingMillis(UUID uuid) {
        if (!isExempted(uuid)) {
            return 0L;
        }
        long expiresAt = this.exemptedPlayers.get(uuid);
        return expiresAt == 0L ? -1L : Math.max(0L, expiresAt - System.currentTimeMillis());
    }
}