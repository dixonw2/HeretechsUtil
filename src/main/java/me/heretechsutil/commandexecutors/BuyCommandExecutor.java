package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.BuyableEntity;
import me.heretechsutil.operations.ConfigOperations;
import me.heretechsutil.operations.DatabaseOperations;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BuyCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        String methodTrace = "BuyCommandExecutor.onCommand():";
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args.length == 2 && args[0].equalsIgnoreCase("view")) {
                ArrayList<String> buyables = new ArrayList<>();
                // args[1] is the category
                ArrayList<BuyableEntity> buyablesForCategory = ConfigOperations.getBuyables(args[1]);
                if (buyablesForCategory != null && buyablesForCategory.size() > 0) {
                    buyablesForCategory.forEach(x -> {
                        String itemName = WordUtils.capitalizeFully(x.getItem()) + ": " + x.getCost();
                        itemName = itemName.replaceAll("_", " ");
                        buyables.add(itemName);
                    });

                    Collections.sort(buyables);
                    boolean aqua = false;
                    StringBuilder s = new StringBuilder();
                    for (int i = 0; i < buyables.size(); i++) {
                        s.append(aqua ? ChatColor.AQUA : ChatColor.YELLOW).append(buyables.get(i));
                        aqua = !aqua;
                        if (i + 1 != buyables.size()) {
                            s.append(", ");
                        }
                    }
                    p.sendMessage(String.format("%s===== %s =====", ChatColor.LIGHT_PURPLE, WordUtils.capitalizeFully(args[1])));
                    p.sendMessage(s.toString());
                    p.sendMessage(String.format("%s===== %s =====", ChatColor.LIGHT_PURPLE, WordUtils.capitalizeFully(args[1])));
                    return true;
                }
                else {
                    p.sendMessage(String.format("%sCategory %s either does not exist or does not contain any buyables",
                        ChatColor.RED, WordUtils.capitalizeFully(args[1])));
                    return true;
                }
            }
            else if (args.length >= 2 && args[0].equalsIgnoreCase("item")) {

                if (ConfigOperations.getBuyables(args[1]) == null) {
                    p.sendMessage(String.format("%sCategory [%s] does not exist", ChatColor.RED, WordUtils.capitalizeFully(args[1])));
                    return true;
                }

                int amount = 0;
                double cost = 0;
                StringBuilder item = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    if (args[i].contains(":")) {
                        item.append(args[i].replace(":", ""));
                        try {
                            amount = Integer.parseInt(args[args.length - 1]);
                            double tempCost = Double.parseDouble(args[i + 1]);
                            BuyableEntity buyable = ConfigOperations.getBuyable(args[1], item.toString().replace(" ", "_"));
                            if (buyable != null) {
                                cost = buyable.getCost();
                                if (tempCost != cost) {
                                    p.sendMessage(ChatColor.LIGHT_PURPLE + "Nice try");
                                    return true;
                                }
                                break;
                            }
                            else {
                                p.sendMessage(String.format("%sItem [%s] does not exist", ChatColor.RED, WordUtils.capitalizeFully(item.toString())));
                                return true;
                            }
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
                    p.sendMessage(ChatColor.GREEN + "You've received an additional life. Use it wisely.");
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
        sender.sendMessage(ChatColor.RED + String.format("Usage: /%s {item/view} <category> <item> <quantity>", alias));
        return true;
    }

}
