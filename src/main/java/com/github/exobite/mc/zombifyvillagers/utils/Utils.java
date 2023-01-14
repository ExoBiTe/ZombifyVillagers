package com.github.exobite.mc.zombifyvillagers.utils;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.logging.Level;

public class Utils {

    private static PluginMaster main;

    public static void registerUtils(PluginMaster mainInstance) {
        if(main != null) return;
        main = mainInstance;
    }

    private Utils() {}

    public static boolean updateFileVersionDependent(String filename) {
        if(VersionHelper.isEqualOrLarger(VersionHelper.getBukkitVersion(), new Version(1, 18, 0))) {
            return updateConfigFileWithComments(filename);
        }else{
            boolean changed = fillDefaultFile(filename);
            if(changed) PluginMaster.sendConsoleMessage(Level.INFO, "The Comments from the File "+filename+" may have been deleted.\n" +
                    "Consider using a newer Bukkit Version (1.18+) to prevent this issue.");
            return changed;
        }
    }

    public static boolean updateConfigFileWithComments(String filename) {
        File f = new File(main.getDataFolder()+File.separator+filename);
        boolean changedFile = false;
        if(!f.exists()) {
            main.saveResource(filename, true);
            changedFile = true;
        }else{
            YamlConfiguration conf = YamlConfiguration.loadConfiguration(f);
            YamlConfiguration defaultConf = getDefaultConfiguration(filename);
            //Iterate through all visible keys
            for(String key:defaultConf.getKeys(true)) {
                if(conf.get(key)==null) {
                    conf.set(key, defaultConf.get(key));
                    conf.setComments(key, defaultConf.getComments(key));
                    conf.setInlineComments(key, defaultConf.getInlineComments(key));
                    changedFile = true;
                }
            }
            if(changedFile) {
                try {
                    conf.save(f);
                } catch (IOException e) {
                    PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't update the File "+filename+"!");
                    e.printStackTrace();
                }
            }
        }
        return changedFile;
    }

    private static YamlConfiguration getDefaultConfiguration(String filename) {
        InputStream is = main.getResource(filename);
        if(is==null) {
            //Is handled with a runnable, as it is unknown in which Thread we are.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().disablePlugin(main);
                }
            }.runTask(main);
            throw new IllegalArgumentException("Embedded File "+filename+" not found!\nIs the Jar Modified?");
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(is));
    }

    private static boolean fillDefaultFile(String filePath) {
        if(main==null) return false;
        File f = new File(main.getDataFolder()+File.separator+filePath);
        boolean change = false;
        if(!f.exists()) {
            main.saveResource(filePath, true);
            return true;
        }
        InputStream is = getResource(filePath);
        if(is==null) {
            PluginMaster.sendConsoleMessage(Level.SEVERE, "Couldn't find "+filePath+" in project files.");
            return false;
        }
        InputStreamReader rd = new InputStreamReader(is);
        FileConfiguration fcDefault = YamlConfiguration.loadConfiguration(rd);
        FileConfiguration fc = YamlConfiguration.loadConfiguration(f);
        for(String key:fcDefault.getKeys(true)) {
            if(!fc.contains(key)) {
                change = true;
                fc.set(key, fcDefault.get(key));
            }
        }
        if(change) {
            //Save FileConfig to file
            try {
                fc.save(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return change;
    }

    public static InputStream getResource(String filename) {
        if (main == null) {
            throw new IllegalArgumentException("Main cannot be null");
        }
        return main.getResource(filename);
    }

    public static int countMatches(String toSearch, String match) {
        //Example: "abc.abc.abc.def", "def"
        // length = 15, newLength = 12, diff 3 division by length of match = 1
        return (toSearch.length() - toSearch.replace(match, "").length()) / match.length();
    }



}
