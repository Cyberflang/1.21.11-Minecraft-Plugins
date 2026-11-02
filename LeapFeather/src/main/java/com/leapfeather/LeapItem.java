package com.leapfeather;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class LeapItem {

    private final NamespacedKey key;

    LeapItem(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, "leap_feather");
    }

    public ItemStack create(LeapSettings s) {
        ItemStack item = new ItemStack(Material.FEATHER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Text.mini(s.itemName).decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        for (String line : s.itemLore) {
            lore.add(Text.mini(line).decoration(TextDecoration.ITALIC, false));
        }
        meta.lore(lore);

        if (s.itemGlow) {
            meta.setEnchantmentGlintOverride(true);
        }
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

        item.setItemMeta(meta);
        return item;
    }

    public boolean isLeap(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER || !item.hasItemMeta()) {
            return false;
        }
        return item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
