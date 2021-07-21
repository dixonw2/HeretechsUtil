package me.heretechsutil.operations;

import jdk.jfr.internal.LogLevel;
import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.BuyableEntity;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.logging.Level;

public class ConfigOperations {
    private static final ArrayList<BuyableEntity> buyables = new ArrayList<>();

    public static BuyableEntity getBuyable(String item) {
        if (item.equalsIgnoreCase("life")) {
            return buyables.stream().filter(x -> x.getItem().equalsIgnoreCase(item)).findFirst().get();
        }
        return buyables.stream().filter(x -> x.getItem().equalsIgnoreCase(Material.matchMaterial(item).name())).findFirst().get();
    }

    public static ArrayList<BuyableEntity> getBuyables() {
        return buyables;
    }

    public static void loadBuyables() {
        String methodTrace = "DatabaseOperations.loadBuyables():";
        HashMap<String, Object> items = (HashMap<String, Object>) Objects.requireNonNull(HeretechsUtil.getInstance().getConfig().
                getConfigurationSection("buyables")).getValues(false);
        items.forEach((item, cost) -> {
            Material m = Material.matchMaterial(item);
            if (m != null || item.equalsIgnoreCase("life")) {
                try {
                    double amount = Double.parseDouble(cost.toString());
                    buyables.add(new BuyableEntity(item, amount));
                }
                catch (NumberFormatException e) {
                    HeretechsUtil.getInstance().getLogger().
                        log(Level.SEVERE, String.format("%s Cost for material %s not a valid number", methodTrace, m.name()));
                }
            }
            else {
                HeretechsUtil.getInstance().getLogger().
                    log(Level.SEVERE, String.format("%s %s is not a valid item. Ignored", methodTrace, item));
            }
        });

        buyables.sort(Comparator.comparing(BuyableEntity::getItem));
    }
}
