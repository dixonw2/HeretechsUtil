package me.heretechsutil.commandexecutors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompletePointsCommandExecutor implements TabCompleter {

    private static final String[] subCommands = { "pay", "query" };

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            if (args.length == 1) {
                for (String opt : subCommands) {
                    if (opt.toLowerCase().contains(args[0].toLowerCase()))
                        options.add(opt);
                    }
            }
            else if (args.length == 2) {
                Player p = (Player) sender;
                p.getWorld().getPlayers().stream().filter(x ->
                        x.getName().toLowerCase().contains(args[1].toLowerCase())).
                        forEach(x -> options.add(x.getName()));
                Collections.sort(options);
            }

            return options;
        }
        return null;
    }
}
