package com.daynight.faction;

import com.daynight.DayNightPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FactionManager {
    private final DayNightPlugin plugin;
    private final Map<UUID, Faction> factionMap = new HashMap<>();
    private final Set<UUID> exemptedPlayers = new HashSet<>();
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
            for (String key : this.dataConfig.getStringList("exempted")) {
                try {
                    this.exemptedPlayers.add(UUID.fromString(key));
                } catch (Exception ignored) {
                }
            }
        }
    }

    public void saveData() {
        this.factionMap.forEach((uuid, faction) ->
                this.dataConfig.set("factions." + uuid.toString(), faction.name())
        );
        this.dataConfig.set("exempted", this.exemptedPlayers.stream()
                .map(UUID::toString)
                .collect(java.util.stream.Collectors.toList()));
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
        this.exemptedPlayers.add(uuid);
        this.saveData();
    }

    public void unexempt(UUID uuid) {
        this.exemptedPlayers.remove(uuid);
        this.saveData();
    }

    public boolean isExempted(UUID uuid) {
        return this.exemptedPlayers.contains(uuid);
    }
}