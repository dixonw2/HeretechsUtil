package me.heretechsutil.commandexecutors;

import me.heretechsutil.entities.TaskEntity;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TaskCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("view")) {
                    List<TaskEntity> results = DatabaseOperations.getTasksForPlayer(p, args[1]);
                    // sorting wasn't working using the lambda method
                    List<TaskEntity> sortedResults = new ArrayList<>();
                    results.stream().filter(TaskEntity::getCompleted).forEach(sortedResults::add);
                    results.stream().filter(x -> !x.getCompleted()).forEach(sortedResults::add);
                    sortedResults.forEach(x -> p.sendMessage(String.format("%s%s[%s]%s %s%s: %d point%s",
                        x.getDifficulty().equalsIgnoreCase("progression") ? ChatColor.LIGHT_PURPLE :
                            x.getDifficulty().equalsIgnoreCase("easy") ? ChatColor.GREEN :
                            x.getDifficulty().equalsIgnoreCase("medium") ? ChatColor.YELLOW : ChatColor.RED,
                        x.getCompleted() ? ChatColor.STRIKETHROUGH : "",
                        x.getDifficulty(),
                        ChatColor.AQUA,
                        x.getCompleted() ? ChatColor.STRIKETHROUGH : "",
                        x.getTaskDescription(), x.getPointReward(), x.getPointReward() == 1 ? "" : "s")));
                }
                else if (args[0].equalsIgnoreCase("redeem")) {
                    p.sendMessage(ChatColor.RED + String.format("Usage: /%s {view/redeem} <difficulty> <task>", alias));
                }
                return true;
            }
            else if (args.length >= 3 && args[0].equalsIgnoreCase("redeem")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i != args.length - 1) {
                        sb.append(" ");
                    }
                }
                TaskEntity te = DatabaseOperations.getTaskForPlayer(p, sb.toString());
                if (te != null) {
                    DatabaseOperations.redeemTask(p, te);
                    double pointTotal = DatabaseOperations.getPointsForPlayer(p);
                    p.sendMessage(String.format("%sRedeemed task [%s] for %d point%s. You now have %.2f point%s",
                            ChatColor.AQUA, te.getTaskDescription(), te.getPointReward(),
                            te.getPointReward() == 1 ? "" : "s", pointTotal, pointTotal == 1 ? "" : "s"));
                }
                else {
                    p.sendMessage(String.format("%sYou do not have task [%s], or it does not exist", ChatColor.RED, sb.toString()));
                }
                return true;
            }
            p.sendMessage(ChatColor.RED + String.format("Usage: /%s {view/redeem} <difficulty> <task>", alias));
            return true;
        }
        else {
            sender.sendMessage("The console cannot run this command");
        }
        return false;
    }
}
