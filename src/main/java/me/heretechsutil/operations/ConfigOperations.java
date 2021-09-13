package me.heretechsutil.operations;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.BuyableEntity;
import org.bukkit.Material;

import java.util.*;

public class ConfigOperations {
    private static final HashMap<String, List<BuyableEntity>> buyables = new HashMap<>();

    public static BuyableEntity getBuyable(String category, String item) {

        if (item.equalsIgnoreCase("life")) {
            return buyables.get("buyables.Miscellaneous").stream().filter(x -> x.getItem().equalsIgnoreCase("life")).findFirst().get();
        }
        HeretechsUtil.getInstance().getLogger().info("Category: " + category);
        HeretechsUtil.getInstance().getLogger().info("Item: " + item);
        HeretechsUtil.getInstance().getLogger().info("buyables count: " + buyables.size());
        buyables.forEach((x, y) -> HeretechsUtil.getInstance().getLogger().info("Key: " + x));
        return buyables.get("buyables." + category.toUpperCase()).stream().filter(x -> x.getItem().equalsIgnoreCase(Objects.requireNonNull(Material.matchMaterial(item)).name())).findFirst().get();
    }

    public static HashMap<String, List<BuyableEntity>> getBuyables() {
        return buyables;
    }

    public static void loadBuyables() {
        String methodTrace = "DatabaseOperations.loadBuyables():";
        HashSet<String> categories = (HashSet<String>) HeretechsUtil.getInstance().getConfig().getConfigurationSection("buyables").getKeys(false);

        /*

            Material m = Material.matchMaterial(category);
            if (m != null || category.equalsIgnoreCase("life")) {
                try {
                    double amount = Double.parseDouble(subcategory.toString());
                    buyables.add(new BuyableEntity(category, amount));
                }
                catch (NumberFormatException e) {
                    HeretechsUtil.getInstance().getLogger().
                        log(Level.SEVERE, String.format("%s Cost for material %s not a valid number", methodTrace, m.name()));
                }
            }
            else {
                HeretechsUtil.getInstance().getLogger().
                    log(Level.SEVERE, String.format("%s %s is not a valid item. Ignored", methodTrace, category));
            }
        });

        buyables.sort(Comparator.comparing(BuyableEntity::getItem));*/
        categories.forEach(x -> addBuyableForCategory("buyables." + x));
    }

    private static void addBuyableForCategory(String category) {
        HashMap<String, Object> values = (HashMap<String, Object>) Objects.requireNonNull(HeretechsUtil.getInstance().getConfig().
            getConfigurationSection(category)).getValues(false);
        values.forEach((item, cost) -> {
            if (!buyables.containsKey(category)) {
                buyables.put(category.toUpperCase(), new ArrayList<>());
            }

            buyables.get(category).add(new BuyableEntity(item.toUpperCase(), Double.parseDouble(cost.toString())));
        });
    }
}
