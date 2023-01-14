package com.github.exobite.mc.zombifyvillagers.listener;

import com.github.exobite.mc.zombifyvillagers.PluginMaster;
import com.github.exobite.mc.zombifyvillagers.utils.Config;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("NullableProblems")
public class ZombifyVillagersCommand implements CommandExecutor, TabCompleter {

    private static final String CHANGE_RATE_PERM = "zombifyvillagers.cmd.changeInfectionRate";
    private static final String RELOAD_CFG_PERM = "zombifyvillagers.cmd.reloadConfig";

    private static final String NO_PERMISSION = ChatColor.RED + "You have no permission for this command.";


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(!sender.hasPermission(CHANGE_RATE_PERM) && !sender.hasPermission(RELOAD_CFG_PERM)) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }
        if(args.length<1) {
            sender.sendMessage(getDefaultCommandUsage(sender));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reloadconfig" -> {
                return reloadConfigCmd(sender);
            }

            case "changeinfectionrate" -> {
                return changeInfectionRateCmd(sender, args);
            }

            default -> {
                sender.sendMessage(getDefaultCommandUsage(sender));
                return true;
            }
        }
    }

    private boolean changeInfectionRateCmd(CommandSender sender, String[] args) {
        if(!sender.hasPermission(CHANGE_RATE_PERM)) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }
        if(args.length==1) {
            sender.sendMessage(
                    ChatColor.GRAY+"The current Infection Rate is "+ChatColor.AQUA+Config.getInstance().getInfectionChance()+ChatColor.GRAY+"!" +
                            "\nTo change it, use "+ChatColor.GOLD+"/ZombifyVillagers changeInfectionRate <rate>");
        }else{
            double val;
            try {
                val = Double.parseDouble(args[1]);
            }catch(NumberFormatException e) {
                sender.sendMessage(ChatColor.RED+"'"+args[1]+"' is not a Number! Specify a Number from 0.0 to 1.0!");
                return true;
            }
            if(val < 0 || val > 1) {
                sender.sendMessage(ChatColor.RED + "'" + args[1] + "' is not valid, specify a Number from 0.0 to 1.0!");
                return true;
            }
            double finalVal = val;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Config.getInstance().setInfectionChance(finalVal);
                    boolean success = Config.getInstance().writeCurrentConfigToFile();
                    if(success) {
                        sender.sendMessage(ChatColor.GREEN+"Infection Chance has been set to "+finalVal+"!");
                    }else{
                        sender.sendMessage(ChatColor.RED+"An error has occurred while writing the data.\n" +
                                "Check the console log for further information.");
                    }
                }
            }.runTaskAsynchronously(PluginMaster.getInstance());
            sender.sendMessage(ChatColor.GREEN+"Changing Infection Chance to "+args[1]+"...");
        }
        return true;
    }

    private boolean reloadConfigCmd(CommandSender sender) {
        if(!sender.hasPermission(RELOAD_CFG_PERM)) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                String changes = Config.getInstance().reloadConfiguration();
                sender.sendMessage(ChatColor.GRAY + changes + ChatColor.GREEN + "\nConfig has been reloaded!");
            }
        }.runTaskAsynchronously(PluginMaster.getInstance());
        sender.sendMessage(ChatColor.GREEN+"Reloading Config...");
        return true;
    }

    private String getDefaultCommandUsage(CommandSender sender) {
        StringBuilder sb = new StringBuilder().append(ChatColor.GRAY).append("Command Usage:\n");
        sb.append(ChatColor.GOLD).append("/ZombifyVillagers ");
        boolean alreadyAddedSomething = false;
        if (sender.hasPermission(CHANGE_RATE_PERM)) {
            sb.append("<");
            alreadyAddedSomething = true;
            sb.append("changeInfectionRate");
        }
        if (sender.hasPermission(RELOAD_CFG_PERM)) {
            sb.append(alreadyAddedSomething ? " | " : "<");
            //If more Commands get added, don't forget to add 'alreadyAddedSomething = true;' here
            sb.append("reloadConfig");
        }
        sb.append(">");
        return sb.toString();
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        List<String> data = new ArrayList<>();
        if(args.length<2) {
            if(sender.hasPermission(CHANGE_RATE_PERM)) data.add("changeInfectionRate");
            if(sender.hasPermission(RELOAD_CFG_PERM)) data.add("reloadConfig");
        }
        return data;
    }
}
