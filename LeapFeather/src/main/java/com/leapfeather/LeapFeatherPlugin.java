package com.leapfeather;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class LeapFeatherPlugin extends JavaPlugin {

    private LeapSettings settings;
    private LeapItem leapItem;
    private LeapManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.settings = new LeapSettings(getConfig());
        this.leapItem = new LeapItem(this);
        this.manager = new LeapManager(this);

        getServer().getPluginManager().registerEvents(new LeapListener(this), this);

        PluginCommand cmd = getCommand("leap");
        if (cmd != null) {
            LeapCommand handler = new LeapCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }
        getLogger().info("LeapFeather enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.shutdown();
    }

    public void reloadSettings() {
        reloadConfig();
        this.settings = new LeapSettings(getConfig());
    }

    public LeapSettings settings() { return settings; }
    public LeapItem leapItem() { return leapItem; }
    public LeapManager manager() { return manager; }
}
