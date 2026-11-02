package com.leapfeather;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class LeapListener implements Listener {

    private final LeapFeatherPlugin plugin;

    LeapListener(LeapFeatherPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isLeap(ItemStack item) {
        return plugin.leapItem().isLeap(item);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (!isLeap(e.getItem())) return;

        e.setCancelled(true);

        Player p = e.getPlayer();
        if (!p.hasPermission("leap.use")) return;
        plugin.manager().leap(p);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFall(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (e.getEntity() instanceof Player p && plugin.manager().hasNoFall(p)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (plugin.manager().resumeOnJoin(p)) return;
        if (!plugin.settings().giveOnJoin || !p.hasPermission("leap.use")) return;
        if (!plugin.manager().hasLeap(p)) plugin.manager().give(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!plugin.settings().giveOnRespawn || !p.hasPermission("leap.use")) return;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (p.isOnline() && !plugin.manager().isRecharging(p) && !plugin.manager().hasLeap(p)) {
                plugin.manager().give(p);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.manager().handleQuit(e.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.settings().lockItem && isLeap(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent e) {
        if (plugin.settings().lockItem && (isLeap(e.getMainHandItem()) || isLeap(e.getOffHandItem()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (plugin.settings().lockItem) {
            e.getDrops().removeIf(this::isLeap);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.settings().lockItem) return;
        if (e.getView().getTopInventory().getType() == InventoryType.CRAFTING) return;

        boolean touches = isLeap(e.getCurrentItem()) || isLeap(e.getCursor());

        if (e.getClick().isKeyboardClick() && e.getHotbarButton() >= 0) {
            touches |= isLeap(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()));
        }
        if (e.getClick() == ClickType.SWAP_OFFHAND) {
            touches |= isLeap(e.getWhoClicked().getInventory().getItemInOffHand());
        }
        if (touches) e.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!plugin.settings().lockItem || !isLeap(e.getOldCursor())) return;
        Inventory top = e.getView().getTopInventory();
        if (top.getType() == InventoryType.CRAFTING) return;
        for (int raw : e.getRawSlots()) {
            if (raw < top.getSize()) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
