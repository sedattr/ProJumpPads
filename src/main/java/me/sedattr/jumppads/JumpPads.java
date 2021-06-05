package me.sedattr.jumppads;

import me.sedattr.jumppads.other.Placeholders;
import me.sedattr.jumppads.other.UpdateChecker;
import me.sedattr.jumppads.other.Variables;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ConstantConditions")
public class JumpPads extends JavaPlugin {
    public static JumpPads plugin;

    public void reloadJumpPads() {
        if (!Variables.jumpPads.isEmpty())
            Variables.jumpPads.forEach(pad -> pad.delete(true));

        this.loadJumpPads();
    }

    public void loadJumpPads() {
        Variables.jumpPads = new ArrayList<>();

        File folder = new File(this.getDataFolder() + File.separator + "List");
        File[] files = folder.listFiles();
        if (files == null || files.length <= 0)
            return;

        List<PadHandler> pads = new ArrayList<>();
        for (File file : files) {
            if (!file.getName().contains(".yml"))
                continue;

            pads.add(new PadHandler(file));
        }

        Variables.jumpPads = pads;
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.saveDefaultConfig();

        Variables.messages = this.getConfig().getConfigurationSection("messages");
        Variables.settings = this.getConfig().getConfigurationSection("settings");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            new Placeholders().register();

        this.getCommand("jumppads").setExecutor(new Commands());
        Bukkit.getPluginManager().registerEvents(new Events(), this);

        Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §eJump pads will load after 10 seconds...");
        new BukkitRunnable() {
            @Override
            public void run() {
                JumpPads.this.loadJumpPads();
                Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §aLoaded §f"+ Variables.jumpPads.size() + " §ajump pad successfully!");


                try {
                    new UpdateChecker();
                } catch (IOException ignored) {
                }
            }
        }.runTaskLaterAsynchronously(this, 200);

        new Metrics(this, 11262);
    }

    @Override
    public void onDisable() {
        if (!Variables.jumpPads.isEmpty())
            Variables.jumpPads.forEach(pad -> pad.delete(true));

        Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §cPlugin is disabled & saved jump pads to data!");
    }
}
