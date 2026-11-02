package com.leapfeather;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

final class Text {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {}

    static Component mini(String input) {
        return MM.deserialize(input == null ? "" : input);
    }
}
