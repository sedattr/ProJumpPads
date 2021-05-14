package me.sedattr.jumppads.api;

import me.sedattr.jumppads.PadHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class JumpPadLaunchEvent extends Event implements Cancellable {
    private final Player player;
    private final PadHandler jumpPad;
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    public JumpPadLaunchEvent(Player player, PadHandler jumpPad) {
        this.player = player;
        this.jumpPad = jumpPad;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public PadHandler getJumpPad() {
        return this.jumpPad;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
