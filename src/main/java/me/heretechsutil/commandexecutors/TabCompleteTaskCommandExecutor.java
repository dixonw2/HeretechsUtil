package me.heretechsutil.commandexecutors;

import me.heretechsutil.entities.TaskEntity;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class TabCompleteTaskCommandExecutor implements TabCompleter {

    private static final String[] subCommandsTasks = { "view", "redeem" };
    private static final String[] subCommandsViewTasks = { "easy", "medium", "hard", "all" };

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();

        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (args.length == 1) {
                for (String opt : subCommandsTasks) {
                    if (opt.toLowerCase().contains(args[0].toLowerCase()))
                        options.add(opt);
                }
                return options;
            }
            else if (args.length == 2) {
                for (String opt : subCommandsViewTasks) {
                    if (opt.toLowerCase().contains(args[1].toLowerCase()))
                        options.add(opt);
                }
                return options;
            }
            else if (args.length >= 3 && args[0].equalsIgnoreCase("redeem")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(args[i]);
                    if (i != args.length - 1) {
                        sb.append(" ");
                    }
                }
                options.addAll(DatabaseOperations.getTasksForPlayer(p, args[1]).stream().filter(x -> !x.getCompleted()).
                    map(TaskEntity::getTaskDescription).filter(taskDescription ->
                    taskDescription.toLowerCase().contains(sb.toString().toLowerCase())).collect(Collectors.toList()));
                Collections.sort(options);
            }
        }

        return options;
    }
}
