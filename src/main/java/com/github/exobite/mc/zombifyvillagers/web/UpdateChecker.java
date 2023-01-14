package com.github.exobite.mc.zombifyvillagers.web;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import com.github.exobite.mc.zombifyvillagers.utils.VersionHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;

public class UpdateChecker {

    private static UpdateChecker instance;

    public static UpdateChecker getInstance(){
        return instance;
    }

    public static void createUpdateChecker(JavaPlugin main, boolean useBukkitAsyncScheduler) {
        if(instance==null){
            instance = new UpdateChecker(main);
            instance.start(useBukkitAsyncScheduler);
        }
    }

    private static final String MY_USER_AGENT = "ExobitePlugin";
    private static final int RESOURCE_ID = 107370;
    private static final String GET_LATEST_VERSION = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/versions/latest";
    private static final String GET_LATEST_UPDATE = "https://api.spiget.org/v2/resources/"+RESOURCE_ID+"/updates?size=1&sort=-date";

    private final JavaPlugin main;

    private String latestVersion = null;
    private boolean updateAvailable = false;

    private UpdateChecker(JavaPlugin main) {
        this.main = main;
    }

    private void start(boolean useBukkitAsyncScheduler) {
        BukkitRunnable br = new BukkitRunnable() {
            @Override
            public void run() {
                if(checkForNewerVersion()) {
                    updateAvailable = true;
                    //Send the Message on the Main thread for a nicer-looking console prefix.
                    final String updateTitle = getLatestUpdateTitle();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            StringBuilder sb = new StringBuilder("A new Version (v").append(latestVersion).append(") is available!");
                            if(updateTitle!=null) {
                                final String lines = "---------------------------------";
                                sb.append("\n").append(lines);
                                sb.append("\nUpdate Content:\n  \"").append(updateTitle).append("\"");
                                sb.append("\n").append(lines);
                            }
                            PluginMaster.sendConsoleMessage(Level.INFO, sb.toString());
                        }
                    }.runTask(main);
                }
            }
        };
        if(useBukkitAsyncScheduler) {
            br.runTaskAsynchronously(main);
        }else{
            br.runTask(main);
        }
    }

    private boolean checkForNewerVersion() {
        String currentVersion = main.getDescription().getVersion();
        try {
            URL url = new URL(GET_LATEST_VERSION);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't check for a newer Update, HTTP Error code: "+responseCode);
                return false;
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            JsonElement e = ReflectionHelper.getInstance().parseReader(isr);

            latestVersion = unpackVersionFromJson(e);

            if(latestVersion==null) {
                PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't find the Latest Version in the HTTP Response.");
                return false;
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            latestVersion = "0.0.0";
        }

        return VersionHelper.isLarger(VersionHelper.getVersionFromString(latestVersion), VersionHelper.getVersionFromString(currentVersion));
    }

    private String unpackVersionFromJson(JsonElement e){
        if(!(e instanceof JsonObject jo)) return null;
        return jo.get("name").getAsString();
    }

    private String getLatestUpdateTitle() {
        String rVal = null;
        try {
            URL url = new URL(GET_LATEST_UPDATE);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.addRequestProperty("User-Agent", MY_USER_AGENT);

            int responseCode = con.getResponseCode();
            if(responseCode >= 300) {
                //Ignore Response, as this occurs when no Resource Update has been posted (yet).
                if(responseCode != 404) {
                    PluginMaster.sendConsoleMessage(Level.WARNING, "Couldn't check for the latest Update, HTTP Error code: "+responseCode);
                }
                return null;
            }

            InputStream is = con.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            JsonElement e = ReflectionHelper.getInstance().parseReader(isr);

            if(e instanceof JsonArray ja) {
                JsonObject jo = (JsonObject) ja.get(0);
                rVal = jo.get("title").getAsString();
            }else{
                rVal = "Some Error happened by parsing the Update Title.";
            }

            isr.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rVal;
    }


    public boolean isUpdateAvailable(){
        return updateAvailable;
    }

    public String getLatestVersion(){
        return latestVersion;
    }

}
