package me.sedattr.jumppads;

import me.sedattr.jumppads.other.Variables;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

public class Events implements Listener {
    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        Player player = (Player) e.getEntity();
        if (!Variables.isJumping.containsKey(player))
            return;

        new BukkitRunnable() {
            @Override
            public void run() {
                player.eject();
                e.getDismounted().setPassenger(player);
            }
        }.runTaskLater(JumpPads.plugin, 1);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        if (from.getWorld() == null)
            return;
        Location to = e.getTo();
        if (to == null || to.getWorld() == null)
            return;

        if (!from.getWorld().getName().equalsIgnoreCase(to.getWorld().getName()) || from.distance(to) > 10.0D)
            Variables.isJumping.remove(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        Player player = (Player) e.getEntity();
        if (!Variables.isJumping.containsKey(player))
            return;

        e.setCancelled(true);
    }
}
