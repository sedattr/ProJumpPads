package me.sedattr.jumppads.other;

import fr.mrmicky.fastparticle.FastParticle;
import fr.mrmicky.fastparticle.ParticleType;
import me.sedattr.jumppads.PadHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static void sendSound(Player player, String text) {
        if (!player.isOnline())
            return;

        ConfigurationSection section = Variables.settings.getConfigurationSection("sounds." + text);
        if (section == null)
            return;
        if (!section.getBoolean("enabled"))
            return;

        String name = section.getString("name");
        if (name == null || name.equals(""))
            return;

        Sound sound = Sound.valueOf(name);
        player.playSound(player.getLocation(), sound, Math.max(section.getInt("volume"), 1), (float) Math.max(section.getDouble("pitch"), 1));
    }

    public static void sendMessage(CommandSender player, String text, Boolean list) {
        if (list) {
            List<String> messages = Variables.messages.getStringList(text);
            if (messages.isEmpty())
                return;

            for (String message : messages)
                player.sendMessage(colorize(message));
        } else
            player.sendMessage(colorize(Variables.messages.getString(text)));
    }

    public static String colorize(String s) {
        if (s == null || s.equals("")) return "";
        if (!Bukkit.getVersion().contains("1.16"))
            return ChatColor.translateAlternateColorCodes('&', s);

        Pattern pattern = Pattern.compile("#[a-fA-F0-9]{6}");
        Matcher match = pattern.matcher(s);
        while (match.find()) {
            String hexColor = s.substring(match.start(), match.end());
            s = s.replace(hexColor, ChatColor.of(hexColor).toString());
            match = pattern.matcher(s);
        }

        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void showParticle(Player player, String type, Location location) {
        ConfigurationSection section = Variables.settings.getConfigurationSection("particles." + type);
        if (section == null || !section.getBoolean("enabled"))
            return;
        ParticleType effect = ParticleType.valueOf(section.getString("name"));

        if (player == null)
            FastParticle.spawnParticle(location.getWorld(),
                    effect,
                    location,
                    section.getInt("amount"),
                    section.getDouble("x-offset"),
                    section.getDouble("y-offset"),
                    section.getDouble("z-offset"),
                    section.getDouble("speed"));
        else
            FastParticle.spawnParticle(player,
                    effect,
                    location,
                    section.getInt("amount"),
                    section.getDouble("x-offset"),
                    section.getDouble("y-offset"),
                    section.getDouble("z-offset"),
                    section.getDouble("speed"));
    }

    public static void getList(CommandSender p) {
        int count = 1;
        if (Variables.jumpPads.isEmpty()) {
            Utils.sendMessage(p, "no-jump-pad", false);
            return;
        }

        for (PadHandler pad : Variables.jumpPads) {
            p.sendMessage(Utils.colorize(Variables.messages.getString("list", "&a%count%&8- &a%name%")
                    .replace("%count%", String.valueOf(count)).replace("%name%", pad.getName())));
            count++;
        }
    }
}
