package me.heretechsutil.operations;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.BuyableEntity;
import org.bukkit.Material;

import java.util.*;

public class ConfigOperations {
    private static final HashMap<String, ArrayList<BuyableEntity>> buyables = new HashMap<>();

    public static BuyableEntity getBuyable(String category, String item) {

        if (item.equalsIgnoreCase("life")) {
            return buyables.get("Miscellaneous").stream().filter(x -> x.getItem().equalsIgnoreCase("life")).findFirst().orElse(null);
        }
        buyables.forEach((x, y) -> HeretechsUtil.getInstance().getLogger().info("Key: " + x));
        return buyables.get(category.toUpperCase()).stream().filter(x -> x.getItem().equalsIgnoreCase(Objects.requireNonNull(Material.matchMaterial(item)).name())).findFirst().orElse(null);
    }

    public static HashMap<String, ArrayList<BuyableEntity>> getBuyables() {
        return buyables;
    }

    public static ArrayList<BuyableEntity> getBuyables(String category) {
        return buyables.get(category.toUpperCase());
    }

    public static void loadBuyables() {
        String methodTrace = "ConfigOperations.loadBuyables():";
        HashSet<String> categories = (HashSet<String>) HeretechsUtil.getInstance().getConfig().getConfigurationSection("buyables").getKeys(false);
        HeretechsUtil.getInstance().getLogger().info(
            String.format("%s Found %d buyable categor%s", methodTrace, categories.size(), categories.size() == 1 ? "y" : "ies"));
        categories.forEach(ConfigOperations::addBuyableForCategory);
    }

    private static void addBuyableForCategory(String category) {
        String methodTrace = "ConfigOperations.addBuyableForCategory():";
        HashMap<String, Object> values = (HashMap<String, Object>) Objects.requireNonNull(HeretechsUtil.getInstance().getConfig().
            getConfigurationSection("buyables." + category)).getValues(false);

        values.forEach((item, cost) -> {
            if (!buyables.containsKey(category.toUpperCase())) {
                buyables.put(category.toUpperCase(), new ArrayList<>());
            }
            buyables.get(category.toUpperCase()).add(new BuyableEntity(item, Double.parseDouble(cost.toString())));
        });
        HeretechsUtil.getInstance().getLogger().info(
                String.format("%s Found %d buyables for category %s, loaded %d", methodTrace, values.size(), category, buyables.get(category.toUpperCase()).size()));
    }
}
