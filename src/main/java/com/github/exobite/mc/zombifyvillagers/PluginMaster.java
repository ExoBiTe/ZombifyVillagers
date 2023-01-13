package com.github.exobite.mc.zombifyvillagers;

import com.github.exobite.mc.zombifyvillagers.listener.EntityDeath;
import com.github.exobite.mc.zombifyvillagers.utils.Config;
import com.github.exobite.mc.zombifyvillagers.utils.Utils;
import com.github.exobite.mc.zombifyvillagers.web.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class PluginMaster extends JavaPlugin {

    private static final int BSTATS_ID = 17400;

    private static PluginMaster instance;

    public static PluginMaster getInstance() {
        return instance;
    }

    public static void sendConsoleMessage(Level level, String msg){
        String[] parts = msg.split("\n");
        for (String part : parts) {
            instance.getLogger().log(level, part);
        }
    }

    @Override
    public void onEnable() {
        long time1 = System.currentTimeMillis();
        instance = this;
        Utils.registerUtils(this);
        Config.setupConfig(this).loadConfig(false);
        getServer().getPluginManager().registerEvents(new EntityDeath(), this);
        if(Config.getInstance().checkForUpdate()) UpdateChecker.createUpdateChecker(this, true);
        if(Config.getInstance().allowMetrics()) setupBStats();
        sendConsoleMessage(Level.INFO, "Running (took "+(System.currentTimeMillis()-time1)+"ms)!");
    }

    private void setupBStats() {
        new Metrics(this, BSTATS_ID);
    }


}
