package com.daynight.command;

import com.daynight.DayNightPlugin;
import com.daynight.faction.Faction;
import com.daynight.inventory.impl.ChoiceGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AdminCommand implements CommandExecutor, TabCompleter {
    private static final String PREFIX = ChatColor.GOLD + "[DNF] " + ChatColor.RESET;
    private static final String PERM = "daynightfactions.admin";

    private final DayNightPlugin plugin;

    public AdminCommand(DayNightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "changefaction":
                handleChangeFaction(sender, args);
                break;
            case "reset":
                handleReset(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "off":
                handleExempt(sender, args, true);
                break;
            case "on":
                handleExempt(sender, args, false);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleChangeFaction(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dnf changefaction <player> <sun/night>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player '" + args[1] + "' is not online.");
            return;
        }

        Faction faction;
        switch (args[2].toLowerCase()) {
            case "sun":
            case "sun_seeker":
            case "sunseeker":
                faction = Faction.SUN_SEEKER;
                break;
            case "night":
            case "night_stalker":
            case "nightstalker":
                faction = Faction.NIGHT_STALKER;
                break;
            default:
                sender.sendMessage(PREFIX + ChatColor.RED + "Invalid faction. Use 'sun' or 'night'.");
                return;
        }

        this.plugin.getFactionManager().setFaction(target.getUniqueId(), faction);
        this.plugin.getGameTask().resetScale(target.getUniqueId());

        String factionDisplay = faction == Faction.SUN_SEEKER
                ? ChatColor.YELLOW + "☀ Sun-Seeker"
                : ChatColor.DARK_PURPLE + "🌙 Night-Stalker";

        sender.sendMessage(PREFIX + ChatColor.GREEN + "Set " + target.getName() + "'s faction to " + factionDisplay + ChatColor.GREEN + ".");
        target.sendMessage(PREFIX + "Your faction has been changed to " + factionDisplay + ChatColor.RESET + " by an admin.");
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dnf reset <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player '" + args[1] + "' is not online.");
            return;
        }

        this.plugin.getFactionManager().removeFaction(target.getUniqueId());
        this.plugin.getGameTask().resetScale(target.getUniqueId());

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            ChoiceGUI gui = new ChoiceGUI(this.plugin);
            this.plugin.getGuiManager().openGUI(gui, target);
        }, 1L);

        sender.sendMessage(PREFIX + ChatColor.GREEN + "Reset " + target.getName() + "'s faction. They will now see the selection screen.");
        target.sendMessage(PREFIX + ChatColor.YELLOW + "Your faction has been reset by an admin. Please choose a new faction.");
    }

    private void handleReload(CommandSender sender) {
        this.plugin.getConfigManager().reload();
        sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded successfully.");
    }

    private void handleExempt(CommandSender sender, String[] args, boolean exempt) {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Usage: /dnf " + args[0].toLowerCase() + " <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(PREFIX + ChatColor.RED + "Player '" + args[1] + "' is not online.");
            return;
        }
        if (exempt) {
            this.plugin.getFactionManager().exempt(target.getUniqueId());
            this.plugin.getGameTask().resetScale(target.getUniqueId());
            for (org.bukkit.potion.PotionEffect effect : target.getActivePotionEffects()) {
                target.removePotionEffect(effect.getType());
            }
            sender.sendMessage(PREFIX + ChatColor.GREEN + target.getName() + " is now exempt from all faction mechanics.");
            target.sendMessage(PREFIX + ChatColor.GRAY + "You have been exempted from faction mechanics by an admin.");
        } else {
            this.plugin.getFactionManager().unexempt(target.getUniqueId());
            sender.sendMessage(PREFIX + ChatColor.GREEN + target.getName() + "'s faction mechanics have been re-enabled.");
            target.sendMessage(PREFIX + ChatColor.YELLOW + "Your faction mechanics have been re-enabled by an admin.");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- DayNightFactions Admin Commands ---");
        sender.sendMessage(ChatColor.YELLOW + "/dnf changefaction <player> <sun/night>" + ChatColor.GRAY + " - Set a player's faction.");
        sender.sendMessage(ChatColor.YELLOW + "/dnf reset <player>" + ChatColor.GRAY + " - Reset a player's faction choice.");
        sender.sendMessage(ChatColor.YELLOW + "/dnf reload" + ChatColor.GRAY + " - Reload the config.yml.");
        sender.sendMessage(ChatColor.YELLOW + "/dnf off <player>" + ChatColor.GRAY + " - Exempt a player from all faction mechanics.");
        sender.sendMessage(ChatColor.YELLOW + "/dnf on <player>" + ChatColor.GRAY + " - Re-enable faction mechanics for a player.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERM)) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("changefaction", "reset", "reload", "off", "on").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("changefaction") || args[0].equalsIgnoreCase("reset")
                || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("on"))) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("changefaction")) {
            return Arrays.asList("sun", "night").stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}