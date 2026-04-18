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
    }

    public void saveData() {
        this.factionMap.forEach((uuid, faction) ->
                this.dataConfig.set("factions." + uuid.toString(), faction.name())
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
}