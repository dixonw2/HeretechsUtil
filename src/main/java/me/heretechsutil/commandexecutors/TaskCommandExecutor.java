package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.TaskEntity;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TaskCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        String methodTrace = "TaskCommandExecutor.onCommand():";
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
                        x.getDifficulty().equalsIgnoreCase("easy") ? ChatColor.GREEN :
                            x.getDifficulty().equalsIgnoreCase("medium") ? ChatColor.YELLOW : ChatColor.RED,
                        x.getCompleted() ? ChatColor.STRIKETHROUGH : "",
                        x.getDifficulty(),
                        ChatColor.AQUA,
                        x.getCompleted() ? ChatColor.STRIKETHROUGH : "",
                        x.getTaskDescription(), x.getPointReward(), x.getPointReward() == 1 ? "" : "s")));
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
                DatabaseOperations.redeemTask(p, te);
                int pointTotal = DatabaseOperations.getPointsForPlayer(p);
                p.sendMessage(String.format("%sRedeemed task [%s]. You now have %d point%s",
                    ChatColor.AQUA, te.getTaskDescription(), pointTotal, pointTotal == 1 ? "" : "s"));
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
