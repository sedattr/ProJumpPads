package me.sedattr.jumppads;

import me.sedattr.jumppads.api.JumpPadsAPI;
import me.sedattr.jumppads.other.Utils;
import me.sedattr.jumppads.other.Variables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public class Commands implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!Variables.settings.getString("permission").equals("")) {
            if (!commandSender.hasPermission(Variables.settings.getString("permission"))) {
                Utils.sendMessage(commandSender, "no-permission", false);
                return false;
            }
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                JumpPads.plugin.reloadConfig();
                Variables.settings = JumpPads.plugin.getConfig().getConfigurationSection("settings");
                Variables.messages = JumpPads.plugin.getConfig().getConfigurationSection("messages");
                JumpPads.plugin.reloadJumpPads();

                Utils.sendMessage(commandSender, "reloaded", false);
                return true;
            }

            if (args[0].equalsIgnoreCase("list")) {
                Utils.getList(commandSender);
                return true;
            }

            if (!(commandSender instanceof Player)) {
                Utils.sendMessage(commandSender, "not-player", false);
                return false;
            }

            Player p = (Player) commandSender;
            if (args[0].equalsIgnoreCase("guide")) {
                Utils.sendMessage(commandSender, "guide", true);
                return true;
            }

            if (args.length < 2) {
                Utils.sendMessage(commandSender, "player-usage", true);
                return false;
            }

            if (args[0].equalsIgnoreCase("create")) {
                new PadHandler(p, args[1]);
                return true;
            }

            PadHandler pad = JumpPadsAPI.getByName(args[1]);
            if (pad == null) {
                Utils.sendMessage(commandSender, "wrong-pad", false);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "tp":
                case "teleport":
                    Location location = pad.getCenter();
                    if (location == null) {
                        Utils.sendMessage(commandSender, "fly-location-not-set", false);
                        return false;
                    }
                    p.teleport(location.clone().add(0, 10, 0));

                    Utils.sendMessage(commandSender, "teleported", false);
                    return true;
                case "view":
                case "info":
                    pad.getInfo(p);
                    return true;
                case "pos-1":
                case "pos-2":
                case "pos1":
                case "pos2":
                    Set<Material> nullSet = null;
                    Block block = p.getTargetBlock(nullSet, 5);
                    if (block == null || block.getType().equals(Material.AIR)) {
                        Utils.sendMessage(commandSender, "look-to-correct-block", false);
                        return false;
                    }

                    String name = block.getType().name();
                    if (!Variables.settings.getString("block", "SLIME_BLOCK").equalsIgnoreCase(name)) {
                        Utils.sendMessage(commandSender, "look-to-correct-block", false);
                        return false;
                    }

                    if (args[0].equalsIgnoreCase("pos1"))
                        pad.setPos1(block.getLocation());
                    else
                        pad.setPos2(block.getLocation());
                    Utils.sendMessage(commandSender, args[0].toLowerCase() + "-set", false);
                    return true;
                case "set-fly-location":
                case "flylocation":
                case "setflylocation":
                    pad.setFlyLocation(p.getLocation());
                    Utils.sendMessage(commandSender, "fly-location-set", false);
                    return true;
                case "delete":
                case "remove":
                    pad.delete(false);
                    pad.getFile().delete();
                    Variables.jumpPads.remove(pad);

                    Utils.sendMessage(commandSender, "deleted", false);
                    return true;
            }

            if (args.length < 3) {
                Utils.sendMessage(commandSender, "player-usage", true);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "add-command":
                case "addcommand":
                    Utils.sendMessage(commandSender, pad.addCommand(getMessage(args.clone())) ? "added-command" : "already-added", false);
                    return true;
                case "remove-command":
                case "removecommand":
                    Utils.sendMessage(commandSender, pad.removeCommand(getMessage(args.clone())) ? "removed-command" : "not-added", false);
                    return true;
                case "setpermission-1":
                case "setpermission1":
                case "set-permission-1":
                case "setpermission2":
                case "setpermission-2":
                case "set-permission-2":
                    pad.setPermission(args[2], args[0].contains("1") ? 1 : 2);

                    Utils.sendMessage(commandSender, "permission-set", false);
                    return true;
                case "permissionmessage":
                case "permission-message":
                case "setpermissionmessage":
                case "set-permission-message":
                    pad.setPermissionMessage(getMessage(args.clone()));
                    Utils.sendMessage(commandSender, "permission-message-set", false);
                    return true;
            }
        }

        if (commandSender instanceof ConsoleCommandSender)
            Utils.sendMessage(commandSender, "console-usage", true);
        else
            Utils.sendMessage(commandSender, "player-usage", true);
        return false;
    }

    public String getMessage(String[] args) {
        List<String> newArgs = Arrays.asList(args);
        StringBuilder message = new StringBuilder();

        for (String text : newArgs) {
            if (args[0].equalsIgnoreCase(text)) continue;
            if (args[1].equalsIgnoreCase(text)) continue;

            if (newArgs.get(newArgs.size() -1).equalsIgnoreCase(text)) message.append(text);
            else message.append(text).append(" ");
        }

        return message.toString();
    }
}
