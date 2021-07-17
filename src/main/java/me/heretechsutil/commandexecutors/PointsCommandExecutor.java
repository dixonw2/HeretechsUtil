package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PointsCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        String methodTrace = "PointsCommandExecutor.onCommand():";

        if (sender instanceof Player) {
            Player p = (Player) sender;
            List<Player> players = p.getWorld().getPlayers();
            Player target = null;
            if (args.length == 3) {
                for (Player pl : players) {
                    if (pl.getName().equalsIgnoreCase(args[1])) {
                        target = pl.getPlayer();
                        break;
                    }
                }
                if (target != null) {
                    if (p == target) {
                        p.sendMessage(ChatColor.RED + "You can't pay yourself");
                        return true;
                    }
                    try {
                        int amount = Integer.parseInt(args[2]);
                        int currentPlayerPoints = DatabaseOperations.getPointsForPlayer(p);
                        if (amount <= 0) {
                            p.sendMessage(ChatColor.RED + "Amount must be positive");
                            return true;
                        }
                        else if (currentPlayerPoints - amount < 0) {
                            p.sendMessage(ChatColor.RED + "You don't have enough points");
                            return true;
                        }

                        DatabaseOperations.addPointsToPlayer(target, amount);
                        DatabaseOperations.addPointsToPlayer(p, -amount);
                        p.sendMessage(ChatColor.AQUA +
                            String.format("You paid %s %d point%s", target.getName(), amount, amount == 1 ? "" : "s"));
                        p.sendMessage(ChatColor.AQUA +
                            String.format("You now have %d point%s", currentPlayerPoints - amount,
                                currentPlayerPoints - amount == 1 ? "" : "s"));
                        target.sendMessage(ChatColor.AQUA +
                            String.format("%s paid you %d point%s", p.getName(), amount, amount == 1 ? "" : "s"));
                        int targetPoints = DatabaseOperations.getPointsForPlayer(target);
                        target.sendMessage(ChatColor.AQUA +
                            String.format("You now have %d point%s", targetPoints, targetPoints == 1 ? "" : "s"));
                        return true;
                    }
                    catch (NumberFormatException e) {
                        HeretechsUtil.getInstance().getLogger().log(Level.SEVERE,
                            String.format("%s Integer expected for arg2", methodTrace), e);
                        p.sendMessage(ChatColor.RED + String.format("Usage: /%s pay {player} {amount}", alias));
                        return true;
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + String.format("Player %s not found", args[1]));
                    return true;
                }
            }
            else if (args.length == 1 && args[0].equalsIgnoreCase("query")) {
                int points = DatabaseOperations.getPointsForPlayer(p);
                sender.sendMessage(ChatColor.AQUA + String.format("%s has %d points", p.getName(), points));
                return true;
            }
            else if (args.length == 2 && args[0].equalsIgnoreCase("query")) {
                for (Player pl : players) {
                    if (pl.getName().equalsIgnoreCase(args[1])) {
                        target = pl.getPlayer();
                        break;
                    }
                }

                if (target != null) {
                    int points = DatabaseOperations.getPointsForPlayer(target);
                    sender.sendMessage(ChatColor.AQUA + String.format("%s has %d points", target.getName(), points));
                }
                else
                    sender.sendMessage(ChatColor.RED + String.format("Player %s not found", args[1]));

                return true;

            }
            else {
                p.sendMessage(ChatColor.RED + String.format("Usage: /%s {pay/query} <player> <amount>", alias));
                return true;
            }
        }
        else {
            sender.sendMessage("The console cannot run this command");
        }
        return false;
    }
}
