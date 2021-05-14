package me.sedattr.jumppads.other;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.sedattr.jumppads.PadHandler;
import me.sedattr.jumppads.api.JumpPadsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "jumppads";
    }

    @Override
    public @NotNull String getAuthor() {
        return "SedatTR";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public String onRequest(OfflinePlayer offline, @NotNull String identifier) {
        if (!offline.isOnline())
            return null;
        if (identifier.equals(""))
            return null;

        Player player = (Player) offline;
        PadHandler pad = JumpPadsAPI.getByName(identifier.replace("canuse_", ""));
        if (pad == null)
            return null;

        String permission;
        if (pad.getPermission1() != null && !player.hasPermission(pad.getPermission1()))
            permission = pad.getPermission1();
        else if (pad.getPermission2() != null && !player.hasPermission(pad.getPermission2()))
            permission = pad.getPermission2();
        else
            permission = null;

        String message = permission == null ?
                Variables.messages.getString("can-use") : Variables.messages.getString("cannot-use");
        if (message == null || message.equals(""))
            return null;

        return Utils.colorize(message
                .replace("%permission%", permission != null && !permission.equals("") ? permission : "")
                .replace("%name%", pad.getName()));
    }
}
