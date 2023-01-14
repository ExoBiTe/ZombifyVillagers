package com.github.exobite.mc.zombifyvillagers.utils;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

        private class ConfigValue<T> {

            String key;
            T value;
            Class<T> type;

            ConfigValue(String key, T value, Class<T> type) {
                this.key = key;
                this.value = value;
                this.type = type;
                configValues.add(this);
            }

            void setValue(Object o) {
                this.value = type.cast(o);
            }

        }

        private List<ConfigValue<?>> configValues = new ArrayList<>();

        //Config Values
        ConfigValue<Double> infectionChance = new ConfigValue<>("InfectionChance", 0.5, Double.class);
        ConfigValue<Boolean> checkForUpdate = new ConfigValue<>("CheckForUpdate", true, Boolean.class);
        ConfigValue<Boolean> allowMetrics = new ConfigValue<>("AllowMetrics", true, Boolean.class);

        ConfigValue<?> getConfigValueByKey(String key) {
            for(ConfigValue<?> v : configValues) {
                if(v.key.equals(key)) return v;
            }
            return null;
        }

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
        //Compare all Config Values
        for(int i=0;i<currentConf.configValues.size();i++) {
            ConfigValues.ConfigValue<?> cfgOld = currentConf.configValues.get(i);
            ConfigValues.ConfigValue<?> cfgNew = newConf.configValues.get(i);
            compareValues(cfgOld.key, cfgOld.value, cfgNew.value, sb);
        }
        if(sb.toString().length()==0) {
            sb.append("No Differences between the old and new Config have been found.");
        }
        currentConf = newConf;
        return sb.toString();
    }

    private <T> void compareValues(String name, T oldConf, T newConf, StringBuilder sb) {
        if(oldConf.equals(newConf)) return;
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
        for(ConfigValues.ConfigValue<?> value : cv.configValues) {
            value.setValue(conf.get(value.key, value.value));
        }
        //Validation
        cv.infectionChance.setValue(setBounds(0.0, cv.infectionChance.value, 1.0));
        return cv;
    }

    public boolean writeCurrentConfigToFile() {
        File f = new File(mainInstance.getDataFolder() + File.separator + CONF_FILENAME);
        if(!f.exists()) {
            mainInstance.getLogger().log(Level.SEVERE, "Couldn't write to config, no File found.");
            return false;
        }
        YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
        for(String key : conf.getKeys(true)) {
            ConfigValues.ConfigValue<?> cfgVal = currentConf.getConfigValueByKey(key);
            if(cfgVal==null) continue;
            conf.set(key, cfgVal.value);
        }
        try {
            conf.save(f);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setOnlyIfExists(YamlConfiguration conf, String key, Object value) {
        if(conf.get(key) == null) return;
        conf.set(key, value);
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
        return currentConf.infectionChance.value;
    }

    public boolean checkForUpdate() {
        return currentConf.checkForUpdate.value;
    }

    public boolean allowMetrics() {
        return currentConf.allowMetrics.value;
    }

    //Config Setters

    public void setInfectionChance(double infectionChance) {
        currentConf.infectionChance.value = setBounds(0.0, infectionChance, 1.0);
    }

}
