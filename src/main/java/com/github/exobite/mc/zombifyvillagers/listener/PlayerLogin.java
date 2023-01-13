package com.github.exobite.mc.zombifyvillagers.listener;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import com.github.exobite.mc.zombifyvillagers.utils.Config;
import com.github.exobite.mc.zombifyvillagers.web.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerLogin implements Listener {

    private static final String UPDATE_NOTIFICATION_PERM = "zombifyvillagers.notifyOnUpdate";

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(e.getPlayer().hasPermission(UPDATE_NOTIFICATION_PERM) && Config.getInstance().checkForUpdate()
                && UpdateChecker.getInstance().isUpdateAvailable()) {
            String newVersion = UpdateChecker.getInstance().getLatestVersion();
            String currentVersion = PluginMaster.getInstance().getDescription().getVersion();
            e.getPlayer().sendMessage(ChatColor.YELLOW + "A New version of ZombifyVillagers is available!\n" +
                    "You are running Version "+currentVersion+", the latest Version is "+newVersion+"!\n" +
                    "Update at https://www.spigotmc.org/resources/107370/");
        }
    }

}
