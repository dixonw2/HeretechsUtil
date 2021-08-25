package me.heretechsutil.commandexecutors;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.ConfigOperations;
import org.apache.commons.lang.WordUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompleteBuyCommandExecutor implements TabCompleter {

    private static final String[] subCommands = new String[] { "view", "item" };

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (sender instanceof Player) {
            if (args.length == 1) {
                for (String opt : subCommands) {
                    if (opt.toLowerCase().contains(args[0].toLowerCase()))
                        options.add(opt);
                }
            }
            // only allows for one word to be typed for filtering
            else if (args.length >= 1 && args.length <= 2 && args[0].toLowerCase().contains("item")) {
                StringBuilder item = new StringBuilder();
                for (int i = 1; i < args.length; i++) {
                    item.append(args[i]);
                }
                ConfigOperations.getBuyables().stream().filter(x -> x.getItem().toLowerCase().
                    contains(item.toString().toLowerCase())).forEach(x -> {
                        String itemName = WordUtils.capitalizeFully(x.getItem()) + ": " + x.getCost();
                        itemName = itemName.replaceAll("_", " ");
                        options.add(itemName);
                });
            }

            return options;
        }
        return null;
    }
}
