package com.leapfeather;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class LeapSettings {

    final double cooldownSeconds;
    final int cooldownTicks;
    final double horizontalPower;
    final double verticalPower;
    final boolean pitchAffectsDistance;
    final double minPitchFactor;
    final double airMultiplier;
    final double noFallSeconds;

    final boolean soundEnabled;
    final String soundKey;
    final float soundVolume;
    final float soundPitch;

    final boolean trailEnabled;
    final int trailTicks;

    final Set<String> worlds = new HashSet<>();

    final String itemName;
    final List<String> itemLore;
    final boolean itemGlow;
    final int slot;
    final boolean giveOnJoin;
    final boolean giveOnRespawn;
    final boolean lockItem;

    private final FileConfiguration cfg;

    LeapSettings(FileConfiguration cfg) {
        this.cfg = cfg;

        cooldownSeconds = Math.max(0, cfg.getDouble("leap.cooldown-seconds", 15));
        cooldownTicks = (int) Math.round(cooldownSeconds * 20);
        horizontalPower = cfg.getDouble("leap.horizontal-power", 1.2);
        verticalPower = cfg.getDouble("leap.vertical-power", 0.618);
        pitchAffectsDistance = cfg.getBoolean("leap.pitch-affects-distance", true);
        minPitchFactor = Math.max(0, Math.min(1, cfg.getDouble("leap.min-pitch-factor", 0.25)));
        airMultiplier = cfg.getDouble("leap.air-multiplier", 1.0);
        noFallSeconds = Math.max(0, cfg.getDouble("leap.no-fall-damage-seconds", 4));

        soundEnabled = cfg.getBoolean("leap.effects.sound.enabled", true);
        soundKey = cfg.getString("leap.effects.sound.key", "entity.bat.takeoff");
        soundVolume = (float) cfg.getDouble("leap.effects.sound.volume", 1.0);
        soundPitch = (float) cfg.getDouble("leap.effects.sound.pitch", 1.3);

        trailEnabled = cfg.getBoolean("leap.effects.trail.enabled", true);
        trailTicks = Math.max(0, cfg.getInt("leap.effects.trail.ticks", 12));

        for (String w : cfg.getStringList("leap.enabled-worlds")) {
            worlds.add(w.toLowerCase(Locale.ROOT));
        }

        itemName = cfg.getString("item.name", "<green>Leap <gray>[Use]");
        itemLore = cfg.getStringList("item.lore");
        itemGlow = cfg.getBoolean("item.glow", false);
        slot = cfg.getInt("item.slot", 0);
        giveOnJoin = cfg.getBoolean("item.give-on-join", true);
        giveOnRespawn = cfg.getBoolean("item.give-on-respawn", true);
        lockItem = cfg.getBoolean("item.lock-item", true);
    }

    boolean worldAllowed(String worldName) {
        return worlds.isEmpty() || worlds.contains(worldName.toLowerCase(Locale.ROOT));
    }

    String message(String key) {
        return cfg.getString("messages." + key, "");
    }
}
