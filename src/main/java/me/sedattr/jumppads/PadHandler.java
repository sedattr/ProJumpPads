package me.sedattr.jumppads;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import me.sedattr.jumppads.api.JumpPadLandEvent;
import me.sedattr.jumppads.api.JumpPadLaunchEvent;
import me.sedattr.jumppads.other.Utils;
import me.sedattr.jumppads.other.Variables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class PadHandler implements Listener {
    @Getter private File file;
    @Getter private YamlConfiguration config;
    @Getter private String name;
    @Getter private String permission1;
    @Getter private String permission2;
    @Getter private String permissionMessage;
    @Getter private double pos1X;
    @Getter private double pos1Z;
    @Getter private double pos2X;
    @Getter private double pos2Z;
    @Getter private double relativePosition;
    @Getter private Location flyLocation = null;
    @Getter private BukkitTask particleTask = null;
    @Getter private List<String> commands = new ArrayList<>();
    @Getter private final Map<Player, BukkitTask> tasks = new HashMap<>();

    public PadHandler(Player player, String padName) {
        File newFile = new File(JumpPads.plugin.getDataFolder() + File.separator + "List", padName + ".yml");
        if (newFile.exists()) {
            Utils.sendMessage(player, "jump-pad-exists", false);
            return;
        }

        try {
            newFile.createNewFile();
        } catch (IOException ignored) {
        }

        this.file = newFile;
        this.config = YamlConfiguration.loadConfiguration(newFile);
        this.name = padName;
        this.flyLocation = player.getLocation();

        Variables.jumpPads.add(this);
        Bukkit.getPluginManager().registerEvents(this, JumpPads.plugin);
        Utils.sendMessage(player, "created", false);

        this.particles();
        this.save();
    }

    public PadHandler(File newFile) {
        this.file = newFile;
        this.config = YamlConfiguration.loadConfiguration(newFile);
        this.name = newFile.getName().replace(".yml", "");
        this.pos1X = this.config.getDouble("pos1X");
        this.pos1Z = this.config.getDouble("pos1Z");
        this.pos2X = this.config.getDouble("pos2X");
        this.pos2Z = this.config.getDouble("pos2Z");
        this.relativePosition = this.config.getDouble("relative-position");
        this.permissionMessage = this.config.getString("permission-message");
        this.permission1 = this.config.getString("permission-1");
        this.permission2 = this.config.getString("permission-2");
        List<String> cmds = this.config.getStringList("commands");
        if (!cmds.isEmpty())
            this.commands = cmds;

        this.loadFlyLocation();
        this.particles();

        Bukkit.getPluginManager().registerEvents(this, JumpPads.plugin);
    }

    public void loadFlyLocation() {
        ConfigurationSection flyLocation = this.config.getConfigurationSection("fly-location");
        if (flyLocation == null)
            return;
        String worldName = flyLocation.getString("world");
        if (worldName == null || worldName.equals(""))
            return;

        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return;

        this.flyLocation = new Location(world, flyLocation.getDouble("x"), flyLocation.getDouble("y"), flyLocation.getDouble("z"));
    }

    public void particles() {
        final Location loc = this.getCenter();
        if (loc == null)
            return;
        if (this.particleTask != null)
            this.particleTask.cancel();

        this.particleTask = new BukkitRunnable() {
            public void run() {
                Utils.showParticle(null, "jump-pad", loc);
            }
        }.runTaskTimerAsynchronously(JumpPads.plugin, 0L, (long)Math.max(Variables.settings.getInt("particles.jump-pad.time"), 1) * 20L);
    }

    public void save() {
        if (this.flyLocation != null) {
            this.config.set("fly-location.world", this.flyLocation.getWorld().getName());
            this.config.set("fly-location.x", this.flyLocation.getX());
            this.config.set("fly-location.y", this.flyLocation.getY());
            this.config.set("fly-location.z", this.flyLocation.getZ());
        }

        if (this.commands != null && !this.commands.isEmpty())
            this.config.set("commands", this.commands);

        if (this.name != null && !this.name.equals(""))
            this.config.set("name", this.name);

        if (this.pos1X != 0 && this.pos1Z != 0) {
            this.config.set("pos1X", this.pos1X);
            this.config.set("pos1Z", this.pos1Z);
        }

        if (this.pos2X != 0 && this.pos2Z != 0) {
            this.config.set("pos2X", this.pos2X);
            this.config.set("pos2Z", this.pos2Z);
        }

        if (this.relativePosition != 0)
            this.config.set("relative-position", this.relativePosition);

        if (this.permission1 != null && !this.permission1.equals(""))
            this.config.set("permission-1", this.permission1);

        if (this.permission2 != null && !this.permission2.equals(""))
            this.config.set("permission-2", this.permission2);

        if (this.permissionMessage != null && !this.permissionMessage.equals(""))
            this.config.set("permission-message", this.permissionMessage);

        try {
            this.config.save(this.file);
        } catch (IOException ignored) {
        }
    }

    public void delete(Boolean save) {
        if (save)
            this.save();

        HandlerList.unregisterAll(this);
        if (this.particleTask != null)
            this.particleTask.cancel();

        if (this.tasks.isEmpty())
            return;

        this.tasks.forEach((player, bukkitTask) -> {
            player.setNoDamageTicks(0);
            player.teleport(this.flyLocation);

            Variables.isJumping.remove(player);
            bukkitTask.cancel();

            JumpPadLandEvent event = new JumpPadLandEvent(player, this);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;

            if (player.isOnline())
                Utils.sendSound(player, "land");

            if (this.commands != null && !this.commands.isEmpty())
                for (String command : this.commands)
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%player%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString())
                            .replace("%jump-pad%", this.name));

        });
    }

    public Boolean addCommand(String command) {
        if (this.commands.contains(command))
            return false;

        this.commands.add(command);
        return true;
    }

    public Boolean removeCommand(String command) {
        if (!this.commands.contains(command))
            return false;

        this.commands.remove(command);
        return true;
    }

    public void setFlyLocation(Location loc) {
        this.flyLocation = loc;
        this.particles();

        this.save();
    }

    public void setPermission(String perm, int number) {
        final boolean none = perm == null || perm.equals("") || perm.equalsIgnoreCase("none");
        switch (number) {
            case 1:
                if (none)
                    this.permission1 = null;
                else
                    this.permission1 = perm;
                break;
            case 2:
                if (none)
                    this.permission2 = null;
                else
                    this.permission2 = perm;
        }

    }

    public void setPos1(Location loc) {
        this.pos1X = loc.getX();
        this.pos1Z = loc.getZ();
        this.relativePosition = loc.getY();

        this.particles();
    }

    public void setPos2(Location loc) {
        this.pos2X = loc.getX();
        this.pos2Z = loc.getZ();
        this.relativePosition = loc.getY();

        this.particles();
    }

    public void setPermissionMessage(String message) {
        if (message == null || message.equals("") || message.equals("none"))
            this.permissionMessage = null;
        else
            this.permissionMessage = Utils.colorize(message);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (this.isNegative())
            return;

        Location to = e.getTo();
        if (to == null)
            return;
        Player p = e.getPlayer();
        if (Variables.isJumping.containsKey(p))
            return;

        Block downBlock = to.getBlock().getRelative(BlockFace.DOWN);
        if (downBlock.getType().equals(Material.AIR))
            return;

        String name = downBlock.getType().name();
        String blockName = Variables.settings.getString("block", "SLIME_BLOCK");
        if (!blockName.equalsIgnoreCase(name)) {
            downBlock = downBlock.getRelative(BlockFace.DOWN);
            if (downBlock.getType().equals(Material.AIR))
                return;

            name = downBlock.getType().name();
            if (!blockName.equalsIgnoreCase(name))
                return;
        }

        double y = to.getY();
        double relativePosition1 = this.relativePosition - 2;
        double relativePosition2 = this.relativePosition + 2;
        if (y < relativePosition1 || y > relativePosition2)
            return;
        if (!this.isInside(to))
            return;

        if (this.permission1 != null && !this.permission1.equals("") && !p.hasPermission(this.permission1)) {
            this.noPermission(p, this.permission1);
            return;
        }
        if (this.permission2 != null && !this.permission2.equals("") && !p.hasPermission(this.permission2)) {
            this.noPermission(p, this.permission2);
            return;
        }

        this.launch(p);
    }

    public void launch(final Player p) {
        if (this.flyLocation == null) {
            Utils.sendMessage(p, "fly-location-not-set", false);
            return;
        }
        if (!this.flyLocation.getWorld().equals(p.getWorld()))
            return;

        JumpPadLaunchEvent event = new JumpPadLaunchEvent(p, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        final ArmorStand am = (ArmorStand)p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
        am.setVisible(false);
        am.setPassenger(p);
        Variables.isJumping.put(p, am);
        Utils.sendSound(p, "launch");

        Utils.showParticle(p, "travel", p.getEyeLocation());
        BukkitTask task = new BukkitRunnable() {
            final double x3 = PadHandler.this.flyLocation.distance(p.getLocation()) - 0.0D;
            final double x2 = this.x3 / 3.0D;
            final double y3 = Math.abs(PadHandler.this.flyLocation.getY() - p.getLocation().getY()) % 10;
            final double A3 = -((-this.x2 + this.x3) / (-0.0D + this.x2)) * (-0.0D + this.x2 * this.x2) - this.x2 * this.x2 + this.x3 * this.x3;
            final double D3 = -((-this.x2 + this.x3) / (-0.0D + this.x2)) * (-0.0D + this.x2) - this.x2 + this.y3;
            final double a = this.D3 / this.A3;
            final double b = (-0.0D + this.x2 - (-0.0D + this.x2 * this.x2) * this.a) / (-0.0D + this.x2);
            final double c = 0.0D - this.a * 0.0D * 0.0D - this.b * 0.0D;
            double xC = 0.0D;

            public void run() {
                if (p.getNoDamageTicks() <= 0)
                    p.setNoDamageTicks(100);

                if (!p.isOnline()) {
                    p.setNoDamageTicks(0);
                    am.remove();

                    p.teleport(PadHandler.this.flyLocation);

                    Variables.isJumping.remove(p);
                    tasks.remove(p);
                    this.cancel();

                    JumpPadLandEvent event = new JumpPadLandEvent(p, PadHandler.this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;

                    if (PadHandler.this.commands != null && !PadHandler.this.commands.isEmpty())
                        for (String command : PadHandler.this.commands)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                                    .replace("%player%", p.getName())
                                    .replace("%uuid%", p.getUniqueId().toString())
                                    .replace("%jump-pad%", PadHandler.this.name));
                    return;
                }

                if (PadHandler.this.flyLocation.distance(am.getLocation()) <= 5 || !Variables.isJumping.containsKey(p)) {
                    p.setNoDamageTicks(100);
                    am.remove();

                    Variables.isJumping.remove(p);
                    tasks.remove(p);
                    this.cancel();

                    JumpPadLandEvent event = new JumpPadLandEvent(p, PadHandler.this);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        return;

                    Utils.sendSound(p, "land");
                    if (PadHandler.this.commands != null && !PadHandler.this.commands.isEmpty())
                        for (String command : PadHandler.this.commands)
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%player%", p.getName())
                            .replace("%uuid%", p.getUniqueId().toString())
                            .replace("%jump-pad%", PadHandler.this.name));
                    return;
                }

                Utils.sendSound(p, "flying");
                PadHandler.this.moveToward(am, PadHandler.this.yCalculate(this.a, this.b, this.c, this.xC));
                this.xC += 0.84D;
            }
        }.runTaskTimer(JumpPads.plugin, 1L, 1L);
        this.tasks.put(p, task);
    }

    public void noPermission(Player p, String permission) {
        Utils.sendSound(p, "no-permission");
        String newMessage = this.permissionMessage != null ? this.permissionMessage.replace("%permission%", permission) : null;
        if (newMessage != null)
            p.sendMessage(Utils.colorize(newMessage));

        Utils.showParticle(p, "no-permission", p.getEyeLocation());
        ConfigurationSection section = Variables.settings.getConfigurationSection("push");
        if (section == null || !section.getBoolean("enabled"))
            return;

        Location center = this.getCenter();
        if (center == null)
            return;

        Vector push = p.getLocation().toVector().subtract(center.toVector());
        push = push.normalize().multiply(section.getDouble("power"));
        double yLocation = section.getDouble("y");
        if (yLocation > 0.0D)
            push.setY(yLocation);

        p.setVelocity(push);
    }

    private boolean isNegative() {
        return (this.pos1X == 0 && this.pos1Z == 0) || (this.pos2X == 0 && this.pos2Z == 0);
    }

    private boolean isBetween(double v1, double v2, double b) {
        return (b >= v1 && b <= v2) || (b <= v1 && b >= v2);
    }

    private boolean isInside(Location b) {
        return this.isBetween(this.pos1Z, this.pos2Z, b.getZ()) && this.isBetween(this.pos1X, this.pos2X, b.getX());
    }

    private double yCalculate(double a, double b, double c, double x) {
        return (a * x * x) + (x * b + c);
    }

    private void moveToward(Entity player, double yC) {
        Location loc = player.getLocation();
        double x = loc.getX() - this.flyLocation.getX();
        double y = loc.getY() - this.flyLocation.getY() - (Math.max(yC, 0.0D));
        double z = loc.getZ() - this.flyLocation.getZ();
        Vector velocity = (new Vector(x, y, z)).normalize().multiply(-0.8D);
        player.setVelocity(velocity);
    }

    public void getInfo(Player p) {
        List<String> info = Variables.messages.getStringList("info");
        if (info.isEmpty())
            return;

        for (String message : info) {
            p.sendMessage(Utils.colorize(message
                    .replace("%name%", this.name)
                    .replace("%permission-1%", this.permission1 != null ? this.permission1 : Variables.messages.getString("none", "None"))
                    .replace("%permission-2%", this.permission2 != null ? this.permission2 : Variables.messages.getString("none", "None"))
                    .replace("%permission-message%", this.permissionMessage != null ? this.permissionMessage : Variables.messages.getString("none", "None"))
                    .replace("%fly-location%", this.flyLocation != null ? this.flyLocation.getX() + ", " + this.flyLocation.getY() + ", " + this.flyLocation.getZ() : Variables.messages.getString("not-set", "Not Set"))
                    .replace("%position-1%", this.pos1X != 0 ? this.pos1X + ", " + this.pos1Z : Variables.messages.getString("not-set", "Not Set"))
                    .replace("%position-2%", this.pos2X != 0 ? this.pos2X + ", " + this.pos2Z : Variables.messages.getString("not-set", "Not Set"))));
        }
    }

    public Location getCenter() {
        if (this.isNegative())
            return null;
        if (this.flyLocation == null)
            return null;

        double minX = Math.min(this.pos1X, this.pos2X);
        double minZ = Math.min(this.pos1Z, this.pos2Z);
        double x1 = Math.max(this.pos1X, this.pos2X) + 1;
        double z1 = Math.max(this.pos1Z, this.pos2Z) + 1;
        return new Location(this.flyLocation.getWorld(), minX + (x1 - minX) / 2.0D, this.relativePosition + (this.relativePosition + 1 - this.relativePosition) / 2.0D, minZ + (z1 - minZ) / 2.0D);
    }
}
