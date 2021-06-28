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

    @Override
    public List<String> onTabComplete(CommandSender sender,Command cmd, String alias, String[] args) {
        List<String> players = new ArrayList<>();
        if (sender instanceof Player) {
            Player p = (Player) sender;
            p.getWorld().getPlayers().forEach(pl -> players.add(pl.getName()));

            List<String> playerNames = new ArrayList<>();
            if (args.length == 1) {
                for (String pl : players) {
                    if (pl.toLowerCase().contains(args[0].toLowerCase()))
                        playerNames.add(pl);
                }
                Collections.sort(playerNames);
            }
            return playerNames;
        }
        return null;
    }
}
