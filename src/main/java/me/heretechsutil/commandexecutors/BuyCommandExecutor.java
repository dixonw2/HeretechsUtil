package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.ConfigOperations;
import me.heretechsutil.operations.DatabaseOperations;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BuyCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        String methodTrace = "BuyCommandExecutor.onCommand():";
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1 && args[0].equalsIgnoreCase("view")) {
                ArrayList<String> buyables = new ArrayList<>();
                ConfigOperations.getBuyables().forEach(x -> {
                    String itemName = WordUtils.capitalizeFully(x.getItem()) + ": " + x.getCost();
                    itemName = itemName.replaceAll("_", " ");
                    buyables.add(itemName);
                });
                String s = String.join(", ", buyables);
                p.sendMessage(ChatColor.AQUA + s);
                return true;
            }
            else if (args.length >= 2 && args[0].toLowerCase().contains("item")) {
                int amount = 0;
                double cost = 0;
                StringBuilder item = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    if (args[i].contains(":")) {
                        item.append(args[i].replace(":", ""));
                        try {
                            amount = Integer.parseInt(args[args.length - 1]);
                            double tempCost = Double.parseDouble(args[i + 1]);
                            cost = ConfigOperations.getBuyable(item.toString().replace(" ", "_")).getCost();
                            if (tempCost != cost) {
                                p.sendMessage(ChatColor.LIGHT_PURPLE + "Nice try lol");
                            }
                            break;
                        }
                        catch (NumberFormatException e) {
                            p.sendMessage(ChatColor.RED + "Invalid quantity specified");
                            return true;
                        }
                    }
                    item.append(args[i]).append(" ");
                }

                if (amount <= 0) {
                    p.sendMessage(ChatColor.RED + "Quantity must be a positive non-zero integer");
                    return true;
                }

                if (DatabaseOperations.getPlayer(p).getPoints() < cost * amount) {
                    p.sendMessage(ChatColor.RED + "You don't have enough points");
                    return true;
                }

                if (item.toString().equalsIgnoreCase("life")) {
                    DatabaseOperations.addPointsToPlayer(p, -(amount * cost));
                    DatabaseOperations.updatePlayerLives(p, amount);
                    p.sendMessage(ChatColor.AQUA + "You've received an additional life");
                    double points = DatabaseOperations.getPointsForPlayer(p);
                    p.sendMessage(ChatColor.AQUA + String.format("You now have %.2f point%s", points, points == 1 ? "" : "s"));
                }
                else {
                    HeretechsUtil.getInstance().getLogger().info(item.toString());
                    Material m = Material.matchMaterial(item.toString().replace(" ", "_"));
                    if (m != null) {
                        ItemStack itemStack = new ItemStack(m, amount);
                        HashMap<Integer, ItemStack> missingItems = p.getInventory().addItem(itemStack);
                        if (!missingItems.isEmpty()) {
                            Collection<ItemStack> items = missingItems.values();
                            for (ItemStack is : items) {
                                amount -= is.getAmount();
                            }
                            p.sendMessage(ChatColor.YELLOW + "Not all items fit in your inventory. Ignoring excess");
                        }

                        DatabaseOperations.addPointsToPlayer(p, -(amount * cost));
                        p.sendMessage(ChatColor.AQUA + String.format("You've received %d %s%s", amount, item, amount == 1 ? "" : "s"));
                        double points = DatabaseOperations.getPointsForPlayer(p);
                        p.sendMessage(ChatColor.AQUA + String.format("You now have %.2f point%s", points, points == 1 ? "" : "s"));
                    }
                }
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + String.format("Usage: /%s {item/view} <item> <quantity>", alias));
        return true;
    }

}
