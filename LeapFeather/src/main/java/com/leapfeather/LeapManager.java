package com.leapfeather;

import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LeapManager {


    private static final class Recharge {
        final long end;
        final int slot;
        BukkitTask task;

        Recharge(long end, int slot) {
            this.end = end;
            this.slot = slot;
        }
    }

    private final LeapFeatherPlugin plugin;
    private final Map<UUID, Recharge> recharging = new HashMap<>();
    private final Map<UUID, Long> noFallUntil = new HashMap<>();

    LeapManager(LeapFeatherPlugin plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    public void leap(Player p) {
        LeapSettings s = plugin.settings();

        if (p.getGameMode() == GameMode.SPECTATOR || p.isInsideVehicle()) return;
        if (!s.worldAllowed(p.getWorld().getName())) return;

        boolean bypass = p.hasPermission("leap.bypass-cooldown");

        Recharge active = recharging.get(p.getUniqueId());
        if (!bypass && active != null) {
            sendCountdown(p, active.end - System.currentTimeMillis());
            return;
        }
        Vector dir = p.getLocation().getDirection();
        Vector flat = new Vector(dir.getX(), 0, dir.getZ());
        if (flat.lengthSquared() < 1.0E-4) {
            double yaw = Math.toRadians(p.getLocation().getYaw());
            flat = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
        }
        flat.normalize();
        double pitchFactor = 1.0;
        if (s.pitchAffectsDistance) {
            pitchFactor = Math.max(s.minPitchFactor, Math.cos(Math.toRadians(p.getLocation().getPitch())));
        }
        double power = s.horizontalPower * pitchFactor * (p.isOnGround() ? 1.0 : s.airMultiplier);

        p.setVelocity(flat.multiply(power).setY(s.verticalPower));

        if (s.noFallSeconds > 0) {
            noFallUntil.put(p.getUniqueId(), System.currentTimeMillis() + (long) (s.noFallSeconds * 1000));
        }
        if (s.soundEnabled) {
            p.getWorld().playSound(p.getLocation(), s.soundKey, SoundCategory.PLAYERS, s.soundVolume, s.soundPitch);
        }
        if (s.trailEnabled && s.trailTicks > 0) {
            startTrail(p, s.trailTicks);
        }

        if (!bypass && s.cooldownTicks > 0) {
            startRecharge(p, p.getInventory().getHeldItemSlot());
        }
    }

    private void startTrail(Player p, int ticks) {
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (!p.isOnline() || t++ >= ticks) {
                    cancel();
                    return;
                }
                p.getWorld().spawnParticle(Particle.CLOUD, p.getLocation().add(0, 0.2, 0), 4, 0.15, 0.1, 0.15, 0.01);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void startRecharge(Player p, int slot) {
        PlayerInventory inv = p.getInventory();
        if (plugin.leapItem().isLeap(inv.getItem(slot))) {
            inv.setItem(slot, null);
        }

        long end = System.currentTimeMillis() + (long) (plugin.settings().cooldownSeconds * 1000);
        Recharge r = new Recharge(end, slot);
        recharging.put(p.getUniqueId(), r);
        runRecharge(p, r);
    }

    private void runRecharge(Player p, Recharge r) {
        sendCountdown(p, r.end - System.currentTimeMillis());

        r.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }
                long left = r.end - System.currentTimeMillis();
                if (left <= 0) {
                    if (p.isDead()) return;
                    cancel();
                    recharging.remove(p.getUniqueId());
                    place(p, r.slot);
                    p.sendActionBar(Component.empty());
                    return;
                }
                sendCountdown(p, left);
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    private void sendCountdown(Player p, long leftMillis) {
        int seconds = (int) Math.max(1, Math.ceil(leftMillis / 1000.0));
        String text = plugin.settings().message("cooldown").replace("{seconds}", String.valueOf(seconds));
        p.sendActionBar(Text.mini(text));
    }

    public boolean isRecharging(Player p) {
        return recharging.containsKey(p.getUniqueId());
    }


    public boolean resumeOnJoin(Player p) {
        Recharge r = recharging.get(p.getUniqueId());
        if (r == null) return false;
        if (r.end <= System.currentTimeMillis()) {
            recharging.remove(p.getUniqueId());
            place(p, r.slot);
        } else {
            runRecharge(p, r);
        }
        return true;
    }

    public void handleQuit(Player p) {
        noFallUntil.remove(p.getUniqueId());
        Recharge r = recharging.get(p.getUniqueId());
        if (r != null && r.task != null) {
            r.task.cancel();
        }
    }


    public void shutdown() {
        for (Map.Entry<UUID, Recharge> e : recharging.entrySet()) {
            Recharge r = e.getValue();
            if (r.task != null) r.task.cancel();
            Player p = plugin.getServer().getPlayer(e.getKey());
            if (p != null && p.isOnline() && !p.isDead()) {
                place(p, r.slot);
                p.sendActionBar(Component.empty());
            }
        }
        recharging.clear();
    }

    public boolean hasNoFall(Player p) {
        Long until = noFallUntil.get(p.getUniqueId());
        if (until == null) return false;
        if (until < System.currentTimeMillis()) {
            noFallUntil.remove(p.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean hasLeap(Player p) {
        for (ItemStack item : p.getInventory().getContents()) {
            if (plugin.leapItem().isLeap(item)) return true;
        }
        return false;
    }

    public void give(Player p) {
        place(p, plugin.settings().slot);
    }


    private void place(Player p, int slot) {
        if (hasLeap(p)) return;
        ItemStack item = plugin.leapItem().create(plugin.settings());
        PlayerInventory inv = p.getInventory();

        if (slot >= 0 && slot <= 8) {
            ItemStack existing = inv.getItem(slot);
            if (existing == null || existing.getType().isAir()) {
                inv.setItem(slot, item);
                return;
            }
        }
        Map<Integer, ItemStack> leftover = inv.addItem(item);
        for (ItemStack extra : leftover.values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), extra);
        }
    }
}
