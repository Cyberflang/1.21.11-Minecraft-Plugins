package com.leapfeather;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LeapCommand implements CommandExecutor, TabCompleter {

    private final LeapFeatherPlugin plugin;

    LeapCommand(LeapFeatherPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LeapSettings s = plugin.settings();

        if (args.length == 0) {
            sender.sendMessage(Text.mini(s.message("usage")));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "give" -> {
                if (!sender.hasPermission("leap.give")) {
                    sender.sendMessage(Text.mini(s.message("no-permission")));
                    return true;
                }
                Player target = null;
                if (args.length > 1) {
                    target = Bukkit.getPlayerExact(args[1]);
                } else if (sender instanceof Player self) {
                    target = self;
                }
                if (target == null) {
                    sender.sendMessage(Text.mini(s.message("player-not-found")));
                    return true;
                }
                plugin.manager().give(target);
                sender.sendMessage(Text.mini(s.message("given").replace("{player}", target.getName())));
            }
            case "reload" -> {
                if (!sender.hasPermission("leap.reload")) {
                    sender.sendMessage(Text.mini(s.message("no-permission")));
                    return true;
                }
                plugin.reloadSettings();
                sender.sendMessage(Text.mini(plugin.settings().message("reloaded")));
            }
            default -> sender.sendMessage(Text.mini(s.message("usage")));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("leap.give")) out.add("give");
            if (sender.hasPermission("leap.reload")) out.add("reload");
            out.removeIf(o -> !o.startsWith(args[0].toLowerCase(Locale.ROOT)));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("leap.give")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
