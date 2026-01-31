package net.kyrptonaught.quickshulker.config;

import net.minecraft.util.ClickType;

public enum MouseButton {
    LEFT("Left", ClickType.LEFT),
    RIGHT("Right", ClickType.RIGHT),
    ;

    private final String displayName;
    private final ClickType clickType;

    MouseButton(String displayName, ClickType clickType) {
        this.displayName = displayName;
        this.clickType = clickType;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public ClickType getClickType() {
        return clickType;
    }
}
