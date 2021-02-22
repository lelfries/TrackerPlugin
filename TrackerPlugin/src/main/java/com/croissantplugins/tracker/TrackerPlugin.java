package com.croissantplugins.tracker;


import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class TrackerPlugin extends JavaPlugin implements Listener {

    private static final HashMap<Player, Player> targets = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("The Tracker Plugin (version 1.1.9) is activated!");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("track")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage("Please specify who to track.");
                } else {
                    if (args[0].equalsIgnoreCase("stop")) {
                        if (targets.get(player) != null) {
                            Player target = targets.get(player);
                            player.sendMessage("You are no longer tracking " + target.getName());
                            target.sendMessage(player.getName() + " is no longer tracking you!");
                            targets.put(player, null);
                            return true;
                        } else {
                            player.sendMessage("You can't stop tracking someone if you aren't tracking anyone.");
                        }
                        return false;
                    } else {
                        Player target = Bukkit.getPlayerExact(args[0]);
                        if (target != null) {
                            if(target.equals(player)){
                                player.sendMessage("Hey! You can't track yourself!");
                                return true;
                            }
                            if (targets.get(player) != null)
                                targets.get(player).sendMessage(player.getName() + " is no longer tracking you!");
                            targets.put(player, target);
                            target.sendMessage(player.getName() + " is now tracking you!");
                            player.sendMessage("You are now tracking " + target.getName());
                            ItemStack compass = new ItemStack(Material.COMPASS);
                            CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
                            assert compassMeta != null;

                            compassMeta.setDisplayName(target.getName() + "tracker");
                            compassMeta.setLodestoneTracked(false);
                            if (target.getWorld().equals(player.getWorld())) {
                                compassMeta.setLodestone(target.getLocation());
                            }
                            compass.setItemMeta(compassMeta);
                            player.getInventory().addItem(compass);
                            return true;
                        } else {
                            player.sendMessage(args[0] + " is not online right now.");
                            return false;
                        }
                    }
                }
            } else {
                sender.sendMessage("Hey, you aren't supposed to track anyone!");
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent playerRespawnEvent) {
        Player player = playerRespawnEvent.getPlayer();
        Player target = targets.get(player);
        if (target != null) {
            try {
                if (!player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
                    ItemStack compass = new ItemStack(Material.COMPASS);
                    CompassMeta compassMeta = (CompassMeta) compass.getItemMeta();
                    assert compassMeta != null;

                    compassMeta.setDisplayName(target.getName() + " tracker");
                    compassMeta.setLodestoneTracked(false);
                    if (target.getWorld().equals(player.getWorld())) {
                        compassMeta.setLodestone(target.getLocation());
                    }
                    compass.setItemMeta(compassMeta);
                    player.getInventory().addItem(compass);
                }
            } catch (NullPointerException ignored) {
                //Just to prevent massive error message in the console
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent playerInteractEvent) {
        ItemStack item = playerInteractEvent.getItem();
        if (item != null && item.getType().equals(Material.COMPASS)) {
            Player player = playerInteractEvent.getPlayer();
            Player target = targets.get(player);
            if (target != null) {
                try {
                    if (player.getWorld().equals(target.getWorld())) {
                        CompassMeta compassMeta = (CompassMeta) item.getItemMeta();
                        assert compassMeta != null;

                        compassMeta.setDisplayName(target.getName() + " tracker");
                        compassMeta.setLodestone(target.getLocation());
                        compassMeta.setLodestoneTracked(false);
                        item.setItemMeta(compassMeta);
                        player.sendMessage("Tracking " + target.getName());
                    } else {
                        player.sendMessage(target.getName() + " is not in the same world as you.");
                    }
                } catch (Exception ignored) {
                    //Just to prevent massive error message in the console
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent playerLoginEvent) {
        targets.putIfAbsent(playerLoginEvent.getPlayer(), null);
    }

}
