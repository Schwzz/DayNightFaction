package com.daynight.task;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.daynight.DayNightPlugin;
import com.daynight.config.ConfigManager;
import com.daynight.faction.Faction;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameTask extends BukkitRunnable {

    private final DayNightPlugin plugin;
    private final Map<UUID, Integer> dangerSeconds = new HashMap<>();
    private final Map<UUID, Double> dangerScale = new HashMap<>();
    private final Map<UUID, Integer> safeSeconds = new HashMap<>();
    private final Map<UUID, Integer> graceSeconds = new HashMap<>();

    public GameTask(DayNightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ConfigManager cfg = this.plugin.getConfigManager();
        int intervalSeconds = cfg.getDamageIntervalTicks() / 20;
        int recoveryIntervalSeconds = cfg.getSafeZoneRecoveryIntervalTicks() / 20;
        int effectDelaySeconds = cfg.getEffectsDelayTicks() / 20;

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (this.plugin.getFactionManager().isExempted(uuid)) {
                continue;
            }

            if (graceSeconds.containsKey(uuid)) {
                int remaining = graceSeconds.get(uuid) - 1;
                if (remaining <= 0) {
                    graceSeconds.remove(uuid);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.GREEN + "✔ Grace period ended. Good luck!"));
                } else {
                    graceSeconds.put(uuid, remaining);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(ChatColor.AQUA + "⏳ Grace period: " + ChatColor.WHITE + remaining + "s"));
                }
                continue;
            }

            if (!this.plugin.getFactionManager().hasFaction(uuid)) {
                continue;
            }

            Faction faction = this.plugin.getFactionManager().getFaction(uuid);
            int lightLevel = player.getLocation().getBlock().getLightLevel();
            int threshold = cfg.getLightThreshold();

            boolean inDanger = (faction == Faction.SUN_SEEKER && lightLevel < threshold)
                    || (faction == Faction.NIGHT_STALKER && lightLevel >= threshold);

            if (inDanger) {
                safeSeconds.remove(uuid);
                int seconds = dangerSeconds.merge(uuid, 1, Integer::sum);
                double currentDamage = Math.min(dangerScale.getOrDefault(uuid, cfg.getDamageBase()), cfg.getDamageMax());

                if (seconds == 1) {
                    player.sendMessage(ChatColor.RED + "⚠ You have entered a dangerous "
                            + (faction == Faction.SUN_SEEKER ? "dark" : "bright") + " area!");
                    playSound(player, cfg.getDangerSound());
                } else if (cfg.getDangerEscalationSeconds().contains(seconds)) {
                    player.sendMessage(ChatColor.DARK_RED + "⚠ Danger is escalating! Escape now.");
                    playSound(player, cfg.getDangerSound());
                }

                if (seconds % intervalSeconds == 0) {
                    player.damage(currentDamage * 2.0);
                    dangerScale.put(uuid, Math.min(currentDamage + cfg.getDamageIncrement(), cfg.getDamageMax()));
                }

                if (seconds == effectDelaySeconds) {
                    List<String> effects = faction == Faction.SUN_SEEKER
                            ? cfg.getSunSeekerEffects()
                            : cfg.getNightStalkerEffects();
                    applyEffects(player, effects, intervalSeconds);
                } else if (seconds > effectDelaySeconds && seconds % intervalSeconds == 0) {
                    List<String> effects = faction == Faction.SUN_SEEKER
                            ? cfg.getSunSeekerEffects()
                            : cfg.getNightStalkerEffects();
                    applyEffects(player, effects, intervalSeconds);
                }
            } else {
                boolean wasInDanger = dangerSeconds.containsKey(uuid);
                dangerSeconds.remove(uuid);
                dangerScale.remove(uuid);
                int seconds = safeSeconds.merge(uuid, 1, Integer::sum);
                if (wasInDanger) {
                    player.sendMessage(ChatColor.GREEN + "✔ You have reached safety.");
                    playSound(player, cfg.getSafetySound());
                }
                if (cfg.getSafeZoneRecovery() > 0.0 && seconds % recoveryIntervalSeconds == 0 && !player.isDead()) {
                    player.setHealth(Math.min(player.getHealth() + cfg.getSafeZoneRecovery(), player.getMaxHealth()));
                }
                List<String> passives = faction == Faction.SUN_SEEKER
                        ? cfg.getSunSeekerPassives()
                        : cfg.getNightStalkerPassives();
                applyEffects(player, passives, 2);
            }

            String hud = buildHUD(faction, lightLevel, threshold,
                    inDanger ? dangerScale.getOrDefault(uuid, cfg.getDamageBase()) : 0.0, inDanger);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(hud));
        }
    }

    public void resetScale(UUID uuid) {
        dangerSeconds.remove(uuid);
        dangerScale.remove(uuid);
        safeSeconds.remove(uuid);
    }

    public void startGracePeriod(UUID uuid) {
        resetScale(uuid);
        graceSeconds.put(uuid, this.plugin.getConfigManager().getGracePeriodSeconds());
    }

    public int getGraceSeconds(UUID uuid) {
        return graceSeconds.getOrDefault(uuid, 0);
    }

    public int getDangerSeconds(UUID uuid) {
        return dangerSeconds.getOrDefault(uuid, 0);
    }

    public boolean isInDanger(Player player) {
        UUID uuid = player.getUniqueId();
        if (this.plugin.getFactionManager().isExempted(uuid) || !this.plugin.getFactionManager().hasFaction(uuid)) {
            return false;
        }
        Faction faction = this.plugin.getFactionManager().getFaction(uuid);
        int lightLevel = player.getLocation().getBlock().getLightLevel();
        return (faction == Faction.SUN_SEEKER && lightLevel < this.plugin.getConfigManager().getLightThreshold())
                || (faction == Faction.NIGHT_STALKER && lightLevel >= this.plugin.getConfigManager().getLightThreshold());
    }

    private void applyEffects(Player player, List<String> effectDefinitions, int durationSeconds) {
        int durationTicks = (durationSeconds + 2) * 20;
        for (String def : effectDefinitions) {
            String[] parts = def.split(":");
            String effectName = parts[0].trim().toUpperCase();
            int amplifier = parts.length > 1 ? parseInt(parts[1].trim(), 1) - 1 : 0;
            final int amp = amplifier;
            XPotion.matchXPotion(effectName)
                    .map(xp -> xp.buildPotionEffect(durationTicks, amp))
                    .ifPresent(player::addPotionEffect);
        }
    }

    private String buildHUD(Faction faction, int lightLevel, int threshold, double currentDamage, boolean inDanger) {
        if (faction == Faction.SUN_SEEKER) {
            boolean safe = lightLevel >= threshold;
            String levelColor = safe ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
            String damageText = inDanger
                    ? ChatColor.GRAY + " | " + ChatColor.RED + "-" + formatDamage(currentDamage) + "❤"
                    : ChatColor.GRAY + " | " + ChatColor.GREEN + "SAFE";
            return ChatColor.YELLOW + "☀ Light: " + levelColor + lightLevel + ChatColor.GRAY + "/15" + damageText;
        } else {
            boolean safe = lightLevel < threshold;
            String levelColor = safe ? ChatColor.GREEN.toString() : ChatColor.RED.toString();
            String damageText = inDanger
                    ? ChatColor.GRAY + " | " + ChatColor.RED + "-" + formatDamage(currentDamage) + "❤"
                    : ChatColor.GRAY + " | " + ChatColor.GREEN + "SAFE";
            return ChatColor.DARK_PURPLE + "🌙 Darkness: " + levelColor + (15 - lightLevel) + ChatColor.GRAY + "/15" + damageText;
        }
    }

    private String formatDamage(double damage) {
        if (damage == Math.floor(damage)) {
            return String.valueOf((int) damage);
        }
        return String.valueOf(Math.round(damage * 10.0) / 10.0);
    }

    private int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void playSound(Player player, String soundName) {
        XSound.matchXSound(soundName).ifPresent(sound -> sound.play(player));
    }
}