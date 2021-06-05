package me.sedattr.jumppads.other;

import me.sedattr.jumppads.JumpPads;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    public UpdateChecker() throws IOException {
        int projectID = 89782;
        URLConnection con = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectID).openConnection();
        String oldVersion = JumpPads.plugin.getDescription().getVersion();

        InputStreamReader reader = new InputStreamReader(con.getInputStream());
        BufferedReader br = new BufferedReader(reader);
        if (br == null || reader == null) {
            Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §aPlugin is up to date!");
            return;
        }

        String newVersion = br.readLine();
        if (newVersion == null || oldVersion.equals("") || newVersion.equals("") || oldVersion.equalsIgnoreCase(newVersion))
            Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §aPlugin is up to date!");
        else
            Bukkit.getConsoleSender().sendMessage("§8[§bProJumpPads§8] §cNew version found! " + oldVersion + "/" + newVersion);

        br.close();
        reader.close();
    }
}