# 🌗 DayNight Factions: Survival Overhaul

[![Platform](https://img.shields.io/badge/Platform-Spigot%20%2F%20Paper-gold.svg)](https://papermc.io/)
[![Version](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://www.minecraft.net/)
[![License](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

**A high-stakes survival expansion where the light level is your greatest ally—or your deadliest enemy.**

DayNight Factions forces players to choose a side: the **Sun Seekers** or the **Night Stalkers**. Success depends on mastering your environment, as stepping out of your element triggers scaling damage and crippling debuffs.

---

## ⚔️ The Factions

Upon joining, players are presented with a GUI to choose their path. This choice is permanent (unless reset by an admin).

| Feature | ☀️ Sun Seekers | 🌙 Night Stalkers |
| :--- | :--- | :--- |
| **Safe In** | High light levels (Day/Torches) | Low light levels (Night/Caves) |
| **Danger In** | Dark areas / Shadows | Bright areas / Sunlight |
| **Debuffs** | Weakness, Slowness | **Glowing**, Weakness |

---

## ☠️ Danger Zone Mechanics

Staying in the "wrong" light level isn't just uncomfortable—it's fatal.

### 📈 Damage Scaling
The longer you ignore the warning, the faster you die.
* **Initial Hit:** 1.0❤ (1 heart) the moment you enter a danger zone.
* **Scaling:** Damage increases by **+0.5❤** every 5 seconds (configurable).
* **Reset:** Entering safety immediately resets the damage counter to base levels.

### 🧪 Timed Debuffs
After **10 seconds** of exposure, your body begins to fail:
* **Sun Seekers:** Overwhelmed by the dark, suffering from **Weakness** and **Slowness**.
* **Night Stalkers:** Seared by the light, suffering from **Weakness** and the **Glowing** effect (revealing your position to enemies).

### 🌦️ Weather Protection (Bonus)
During **Rain** or **Thunderstorms**, the sky darkens enough for **Night Stalkers** to roam the surface freely during the day without taking damage.

---

## 📊 Minimalist HUD
The plugin features a clean, bar-free Action Bar HUD for real-time survival tracking.

* **In Danger:** `☀ Light: 8/15 | -1.5❤`
* **In Safety:** `☀ Light: 14/15 | SAFE`

---

## 🔧 Configuration
Total control over the difficulty and scaling logic.

```yaml
damage:
  base: 1.0          # Starting damage (hearts)
  increment: 0.5     # Added per interval in danger
  interval-ticks: 100 # How often damage scales (100 ticks = 5s)

light-threshold: 8   # The light level divider

effects:
  delay-ticks: 200   # Delay before debuffs apply (~10s)
  sun-seekers:
    - WEAKNESS:1
    - SLOWNESS:1
  night-stalkers:
    - GLOWING:0
    - WEAKNESS:1
```
## 🔧 Technical Information
* **Server:** Spigot / Paper 1.18 - 1.21+
* **Developer:** [Swartzz](https://github.com/Schwzz)
