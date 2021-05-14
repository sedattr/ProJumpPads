package me.sedattr.jumppads.other;

import me.sedattr.jumppads.PadHandler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Variables {
    public static List<PadHandler> jumpPads = new ArrayList<>();
    public static ConfigurationSection messages;
    public static ConfigurationSection settings;
    public static Map<Player, ArmorStand> isJumping = new HashMap<>();
}
