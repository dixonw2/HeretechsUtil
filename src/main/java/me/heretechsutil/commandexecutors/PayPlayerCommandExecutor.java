package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PayPlayerCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 2) {
                Player target = null;
                List<Player> players = p.getWorld().getPlayers();
                for (Player pl : players) {
                    if (pl.getName().equalsIgnoreCase(args[0])) {
                        target = pl.getPlayer();
                        break;
                    }
                }
                if (target != null) {
                    try {
                        int amount = Integer.parseInt(args[1]);
                        p.sendMessage(ChatColor.AQUA + String.format("You paid %s %s points", args[0], args[1]));
                        return true;
                    }
                    catch (NumberFormatException e) {
                        HeretechsUtil.getInstance().getLogger().log(Level.SEVERE, "PayPlayerCommandExecutor(): " +
                            "Integer expected for arg2", e);
                        p.sendMessage(ChatColor.RED + String.format("Usage: /%s {player} {amount}", alias));
                        return true;
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + String.format("Player %s not found", args[0]));
                    return true;
                }
            }
            else {
                p.sendMessage(ChatColor.RED + String.format("Usage: /%s {player} {amount}", alias));
                return true;
            }
        }
        else {
            sender.sendMessage("The console cannot run this command");
        }
        return false;
    }
}
