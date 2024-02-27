// Importing necessary resources
package me.stormeddeeps.bountysystem;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

// Main plugin class
public final class BountySystem extends JavaPlugin implements Listener {

    // Static variable for Economy
    private static Economy econ = null;
    // HashMap to store bounties
    private HashMap<UUID, Integer> bounties = new HashMap<>();

    // Plugin startup logic
    @Override
    public void onEnable() {
        // Checking if Vault dependency is available
        if (!setupEconomy()) {
            System.out.println(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        // Registering events
        getServer().getPluginManager().registerEvents(this, this);
        System.out.println("Bounty System Has Started. Build Version: 1.0");
    }

    // Plugin shutdown logic
    @Override
    public void onDisable() {
        System.out.println("Bounty System Has Stopped. Build Version: 1.0");
    }

    // Setting up Vault Economy
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    // Command execution logic
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("bounty")) {
            if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
                Player target = Bukkit.getPlayer(args[1]);
                int bounty = Integer.parseInt(args[2]);
                // Checking if sender has enough balance to place bounty
                if (econ.getBalance((Player) sender) >= bounty) {
                    econ.withdrawPlayer((Player) sender, bounty);
                    bounties.put(target.getUniqueId(), bounty);
                    sender.sendMessage("You placed a bounty of " + bounty + " on " + target.getName());
                } else {
                    sender.sendMessage("You do not have enough money to place this bounty.");
                }
            }
        } else if (command.getName().equalsIgnoreCase("bountys")) {
            // Creating inventory to display bounties
            Inventory inv = Bukkit.createInventory(null, 54, "Bounties");

            for (UUID uuid : bounties.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) head.getItemMeta();
                meta.setOwningPlayer(player);
                meta.setDisplayName(player.getName());
                meta.setLore(Arrays.asList("Bounty: " + bounties.get(uuid), "Placed By: " + sender.getName()));
                head.setItemMeta(meta);
                inv.addItem(head);
            }

            ((Player) sender).openInventory(inv);
        }
        return true;
    }

    // Event handler for player death
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Checking if the victim had a bounty
        if (killer != null && bounties.containsKey(victim.getUniqueId())) {
            int bounty = bounties.remove(victim.getUniqueId());
            // Rewarding the killer with the bounty
            econ.depositPlayer(killer, bounty);
            killer.sendMessage("You killed " + victim.getName() + " and received a bounty of " + bounty);
        }
    }
}
