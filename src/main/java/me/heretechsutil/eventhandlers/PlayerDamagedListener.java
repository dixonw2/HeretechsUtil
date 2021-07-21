package me.heretechsutil.eventhandlers;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerDamagedListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            PlayerInventory pi = p.getInventory();

            HeretechsUtil.getInstance().getLogger().info(String.format("Health: %f FinalDamage: %f Lives: %d ", p.getHealth(), event.getFinalDamage(), DatabaseOperations.getPlayer(p).getLives()));
            HeretechsUtil.getInstance().getLogger().info(!pi.getItemInMainHand().isSimilar(new ItemStack(Material.TOTEM_OF_UNDYING)) + "");
            if (p.getHealth() - event.getFinalDamage() <= 0 &&
                !pi.getItemInMainHand().isSimilar(new ItemStack(Material.TOTEM_OF_UNDYING)) &&
                !pi.getItemInOffHand().isSimilar(new ItemStack(Material.TOTEM_OF_UNDYING))) {
                if (DatabaseOperations.getPlayer(p).getLives() <= 0) {
                    HeretechsUtil.getInstance().getLogger().info(String.format("%f %f %d ", p.getHealth(), event.getFinalDamage(), DatabaseOperations.getPlayer(p).getLives()));
                    event.getEntity().getWorld().getPlayers().forEach(pl -> {
                        if (!pl.isDead()) {
                            pl.setHealth(0);
                        }
                    });
                }
                else {
                    HeretechsUtil.getInstance().getLogger().info(String.format("%f %f %d alive", p.getHealth(), event.getFinalDamage(), DatabaseOperations.getPlayer(p).getLives()));
                    event.setCancelled(true);
                    p.setHealth(20);
                    p.sendMessage(ChatColor.DARK_RED + "The Minecraft Gods have bestowed upon you an additional life due to all that you have accomplished. Don't waste it.");
                    p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1, 0.5f);
                    DatabaseOperations.updatePlayerLives(p, -1);
                }
            }
        }
    }
}
