// Importing necessary resources
package me.stormeddeeps.castlecore;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.List;

// Main plugin class
public final class CastleCore extends JavaPlugin implements Listener {

    // List to store reports
    private List<Report> reports = new ArrayList<>();

    // Plugin startup logic
    @Override
    public void onEnable() {
        System.out.println("&a has Successfully Started, Your On Build Version 1.0, No New Builds Released..");
        // Setting command executors
        this.getCommand("sc").setExecutor(new StaffChatCommand(this));
        this.getCommand("report").setExecutor(new ReportCommand(this));
        this.getCommand("reports").setExecutor(new ReportsCommand(this));
        this.getCommand("completereport").setExecutor(new CompleteReportCommand(this));
        // Registering events
        getServer().getPluginManager().registerEvents(this, this);
    }

    // Plugin shutdown logic
    @Override
    public void onDisable() {
        System.out.println("&CastleCore has Stopped And Is Now Shutting Down. Your On build Version: 1.0 No New Builds Released.");
    }

    // CommandExecutor for staff chat command
    public class StaffChatCommand implements CommandExecutor {
        private JavaPlugin plugin;

        public StaffChatCommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Handling command execution
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("castlemc.staffchat")) {
                    if (args.length > 0) {
                        // Building the message
                        StringBuilder sb = new StringBuilder();
                        for (String arg : args) {
                            sb.append(arg).append(" ");
                        }
                        String message = sb.toString().trim();
                        // Sending message to players with staff chat permission
                        for (Player p : plugin.getServer().getOnlinePlayers()) {
                            if (p.hasPermission("castlemc.staffchat")) {
                                p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6&l[STAFF-CHAT] " + "&f" + player.getName() + ": " + message));
                            }
                        }
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "Please provide a message to send to the staff chat.");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return false;
                }
            } else {
                sender.sendMessage("This command can only be run by a player.");
                return false;
            }
        }
    }

    // CommandExecutor for report command
    public class ReportCommand implements CommandExecutor {
        private JavaPlugin plugin;

        public ReportCommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Handling command execution
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("group.default")) {
                    if (args.length >= 2) {
                        // Extracting reported player name and reason from command arguments
                        String reportedPlayerName = args[0];
                        Player reportedPlayer = plugin.getServer().getPlayer(reportedPlayerName);
                        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
                            player.sendMessage(ChatColor.RED + "The player " + reportedPlayerName + " is not online.");
                            return false;
                        }
                        if (player.getName().equalsIgnoreCase(reportedPlayerName)) {
                            player.sendMessage(ChatColor.RED + "You cannot report yourself.");
                            return false;
                        }
                        String reason = String.join(" ", args).substring(reportedPlayerName.length()).trim();
                        // Adding report to the list
                        reports.add(new Report(player.getName(), reportedPlayerName, reason, reports.size() + 1));
                        player.sendMessage(ChatColor.GREEN + "Your report has been filed.");
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "Please provide a player and a reason to report.");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return false;
                }
            } else {
                sender.sendMessage("This command can only be run by a player.");
                return false;
            }
        }
    }

    // CommandExecutor for reports command
    public class ReportsCommand implements CommandExecutor {
        private JavaPlugin plugin;

        public ReportsCommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Handling command execution
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("group.sr-mod")) {
                    // Creating an inventory GUI to display reports
                    Inventory gui = Bukkit.createInventory(player, 54, "Reports");
                    for (Report report : reports) {
                        // Creating ItemStack for The Skull of The Player That Got Reported.
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        meta.setOwner(report.getReportedPlayer());
                        meta.setDisplayName(ChatColor.GOLD + "Report ID: " + report.getId());
                        List<String> lore = new ArrayList<>();
                        lore.add(ChatColor.WHITE + "Reported Player: " + report.getReportedPlayer());
                        lore.add(ChatColor.WHITE + "Reason: " + report.getReason());
                        lore.add(ChatColor.WHITE + "Reported By: " + report.getReporter());
                        meta.setLore(lore);
                        head.setItemMeta(meta);
                        gui.addItem(head);
                    }
                    player.openInventory(gui);
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return false;
                }
            } else {
                sender.sendMessage("This command can only be run by a player.");
                return false;
            }
        }
    }

    // CommandExecutor for completing a report
    public class CompleteReportCommand implements CommandExecutor {
        private JavaPlugin plugin;

        public CompleteReportCommand(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Handling command execution
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("group.sr-mod")) {
                    if (args.length == 1) {
                        try {
                            // Parsing report ID from arguments
                            int id = Integer.parseInt(args[0]);
                            Report reportToRemove = null;
                            // Finding the report to complete
                            for (Report report : reports) {
                                if (report.getId() == id) {
                                    reportToRemove = report;
                                    break;
                                }
                            }
                            if (reportToRemove != null) {
                                // Removing the completed report
                                reports.remove(reportToRemove);
                                player.sendMessage(ChatColor.GREEN + "Report " + id + " has been completed.");
                            } else {
                                player.sendMessage(ChatColor.RED + "No report found with ID " + id + ".");
                            }
                            return true;
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Please provide a valid report ID.");
                            return false;
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Please provide a report ID to complete.");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return false;
                }
            } else {
                sender.sendMessage("This command can only be run by a player.");
                return false;
            }
        }
    }

    // Report class to represent a report
    public class Report {
        private String reporter;
        private String reportedPlayer;
        private String reason;
        private int id;

        public Report(String reporter, String reportedPlayer, String reason, int id) {
            this.reporter = reporter;
            this.reportedPlayer = reportedPlayer;
            this.reason = reason;
            this.id = id;
        }
        public String getReporter() {
            return reporter;
        }

        public String getReportedPlayer() {
            return reportedPlayer;
        }

        public String getReason() {
            return reason;
        }

        public int getId() {
            return id;
        }
    }

    // Event handler for inventory click events
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Reports")) {
            // Canceling the event to prevent manipulation
            event.setCancelled(true);
        }
    }
}
