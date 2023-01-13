package com.github.exobite.mc.zombifyvillagers.utils;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.logging.Level;

public class Config {

    private static final String CONF_FILENAME = "config.yml";

    private static Config instance;

    public static Config getInstance() {
        return instance;
    }

    public static Config setupConfig(JavaPlugin mainInstance) {
        if(instance==null) instance = new Config(mainInstance);
        return instance;
    }
    private static class ConfigValues {

        //Config Values
        double infectionChance = 0.5;
        boolean checkForUpdate = true;
        boolean allowMetrics = true;

    }

    private final JavaPlugin mainInstance;
    private ConfigValues currentConf;

    private Config(JavaPlugin mainInstance) {
        this.mainInstance = mainInstance;
    }

    public void loadConfig(boolean useBukkitAsyncScheduler) {
        if(useBukkitAsyncScheduler) {
            new BukkitRunnable(){

                @Override
                public void run() {
                    currentConf = loadConfiguration();
                }
            }.runTaskAsynchronously(mainInstance);
        }else{
            currentConf = loadConfiguration();
        }
    }

    public String reloadConfiguration() {
        StringBuilder sb = new StringBuilder("");
        ConfigValues newConf = readValuesFromFileConfiguration(loadConfigFromFile());
        compareValues("InfectionChance", currentConf.infectionChance, newConf.infectionChance, sb);
        compareValues("CheckForUpdate", currentConf.checkForUpdate, newConf.checkForUpdate, sb);
        compareValues("AllowMetrics", currentConf.allowMetrics, newConf.allowMetrics, sb);

        if(sb.toString().length()==0) {
            sb.append("No Differences between the old and new Config have been found.");
        }
        currentConf = newConf;
        return sb.toString();
    }

    private <T> void compareValues(String name, T oldConf, T newConf, StringBuilder sb) {
        if(oldConf == newConf) return;
        if(sb.toString().length()==0) sb.append("Found Differences:");  //Header
        sb.append("\n").append(name).append(": ").append(oldConf).append(" --> ").append(newConf);
    }

    private ConfigValues loadConfiguration() {
        YamlConfiguration conf = loadConfigFromFile();
        return readValuesFromFileConfiguration(conf);
    }

    private YamlConfiguration loadConfigFromFile() {
        if(!mainInstance.getDataFolder().exists()) sendInitialStartupMessage();
        File f = new File(mainInstance.getDataFolder() + File.separator + CONF_FILENAME);
        boolean filechanged = Utils.updateFileVersionDependent(CONF_FILENAME);
        if(filechanged) PluginMaster.sendConsoleMessage(Level.INFO, "Your "+CONF_FILENAME+" got updated!");
        return YamlConfiguration.loadConfiguration(f);
    }

    private ConfigValues readValuesFromFileConfiguration(YamlConfiguration conf) {
        ConfigValues cv = new ConfigValues();
        if(conf.getKeys(true).isEmpty()) {
            //No Config or empty Config?
            mainInstance.getLogger().log(
                    Level.SEVERE, "Couldn't load the config.yml, loaded Defaults. Is the File existing and not empty?"
            );
            return cv;
        }
        cv.infectionChance = setBounds(0f, conf.getDouble("InfectionChance", cv.infectionChance), 1.0);
        cv.checkForUpdate = conf.getBoolean("CheckForUpdate", cv.checkForUpdate);
        cv.allowMetrics = conf.getBoolean("AllowMetrics", cv.allowMetrics);
        return cv;
    }

    private double setBounds(double min, double value, double max) {
        return Math.min(Math.max(min, value), max);
    }

    private void sendInitialStartupMessage() {
        final String lines = "---------------------------------";
        String sb = "\n" + lines + "\nWelcome to " + mainInstance.getDescription().getName() + " v" + mainInstance.getDescription().getVersion() + "!\n" +
                """
                The Plugin generated some Configuration Files for you.
                It is Highly advised to change these settings to your liking
                and reboot your server.
                """ + lines;
        mainInstance.getLogger().log(Level.INFO, sb);
    }

    //Config Getters

    public double getInfectionChance() {
        return currentConf.infectionChance;
    }

    public boolean checkForUpdate() {
        return currentConf.checkForUpdate;
    }

    public boolean allowMetrics() {
        return currentConf.allowMetrics;
    }

}
