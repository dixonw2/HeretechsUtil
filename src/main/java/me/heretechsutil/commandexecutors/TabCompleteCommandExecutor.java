package me.heretechsutil.commandexecutors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TabCompleteCommandExecutor implements TabCompleter {

    private static final String[] subCommands = { "pay", "query" };

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            List<String> playerNames = new ArrayList<>();
            if (args.length == 1) {
                for (String opt : subCommands) {
                    if (opt.toLowerCase().contains(args[0].toLowerCase())) {
                        options.add(opt);
                    }
                }
                return options;
            }
            else if (args.length == 2) {
                Player p = (Player) sender;
                p.getWorld().getPlayers().forEach(pl -> options.add(pl.getName()));
                for (String pl : options) {
                    if (pl.toLowerCase().contains(args[1].toLowerCase()))
                        playerNames.add(pl);
                }
                Collections.sort(playerNames);
            }
            return playerNames;
        }
        return null;
    }
}
