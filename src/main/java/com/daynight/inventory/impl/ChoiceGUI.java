package com.daynight.inventory.impl;

import com.daynight.DayNightPlugin;
import com.daynight.faction.Faction;
import com.daynight.inventory.InventoryButton;
import com.daynight.inventory.InventoryGUI;
import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ChoiceGUI extends InventoryGUI {
    private final DayNightPlugin plugin;

    public ChoiceGUI(DayNightPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    protected Inventory createInventory() {
        return Bukkit.createInventory(null, 27, ChatColor.GOLD + "Choose Your Faction");
    }

    @Override
    public void decorate(Player player) {
        ItemStack filler = XMaterial.matchXMaterial("GRAY_STAINED_GLASS_PANE").map(XMaterial::parseItem).orElse(null);
        if (filler != null) {
            ItemMeta fillerMeta = filler.getItemMeta();
            fillerMeta.setDisplayName(" ");
            filler.setItemMeta(fillerMeta);
            for (int i = 0; i < 27; i++) {
                if (i != 11 && i != 15) {
                    this.getInventory().setItem(i, filler);
                }
            }
        }

        this.addButton(11, new InventoryButton()
                .creator(p -> {
                    ItemStack item = XMaterial.matchXMaterial("SUNFLOWER").map(XMaterial::parseItem).orElse(null);
                    if (item == null) return null;
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "☀ Sun-Seeker");
                    meta.setLore(Arrays.asList(
                            ChatColor.GRAY + "Thrive in the light.",
                            ChatColor.GRAY + "Stay at light level &e8+&7 to be safe.",
                            "",
                            ChatColor.RED + "Darkness will slowly drain your life."
                    ));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    this.plugin.getFactionManager().setFaction(clicker.getUniqueId(), Faction.SUN_SEEKER);
                    clicker.closeInventory();
                    this.plugin.getGameTask().startGracePeriod(clicker.getUniqueId());
                    clicker.sendMessage(ChatColor.YELLOW + "☀ You have joined the " + ChatColor.BOLD + "Sun-Seekers" + ChatColor.RESET + ChatColor.YELLOW + "! Stay in the light!");
                })
        );

        this.addButton(15, new InventoryButton()
                .creator(p -> {
                    ItemStack item = XMaterial.matchXMaterial("ENDER_EYE").map(XMaterial::parseItem).orElse(null);
                    if (item == null) return null;
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "🌙 Night-Stalker");
                    meta.setLore(Arrays.asList(
                            ChatColor.GRAY + "Thrive in the darkness.",
                            ChatColor.GRAY + "Stay at light level &57&7 or below to be safe.",
                            "",
                            ChatColor.RED + "Light will slowly drain your life."
                    ));
                    item.setItemMeta(meta);
                    return item;
                })
                .consumer(event -> {
                    Player clicker = (Player) event.getWhoClicked();
                    this.plugin.getFactionManager().setFaction(clicker.getUniqueId(), Faction.NIGHT_STALKER);
                    clicker.closeInventory();
                    this.plugin.getGameTask().startGracePeriod(clicker.getUniqueId());
                    clicker.sendMessage(ChatColor.DARK_PURPLE + "🌙 You have joined the " + ChatColor.BOLD + "Night-Stalkers" + ChatColor.RESET + ChatColor.DARK_PURPLE + "! Embrace the dark!");
                })
        );

        super.decorate(player);
    }
}