package me.heretechsutil.eventhandlers;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerDamagedListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player p = (Player) event.getEntity();
            PlayerInventory pi = p.getInventory();

            if (p.getHealth() - event.getFinalDamage() <= 0 &&
                !pi.getItemInMainHand().isSimilar(new ItemStack(Material.TOTEM_OF_UNDYING)) &&
                !pi.getItemInOffHand().isSimilar(new ItemStack(Material.TOTEM_OF_UNDYING))) {
                if (DatabaseOperations.getPlayer(p).getLives() <= 0) {
                    HeretechsUtil.getInstance().getServer().getOnlinePlayers().forEach(pl -> {
                        if (!pl.isDead() && !pl.getName().equalsIgnoreCase(p.getName())) {
                            pl.setHealth(0);
                        }
                    });
                    for (OfflinePlayer of : HeretechsUtil.getInstance().getServer().getOfflinePlayers()) {
                        Player pl = of.getPlayer();
                        if (pl != null) {
                            if (!pl.isDead() && !pl.getName().equalsIgnoreCase(p.getName())) {
                                pl.setHealth(0);
                            }
                        }
                    }
                }
                else {
                    event.setCancelled(true);
                    p.setHealth(20);

                    if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        if (p.getBedSpawnLocation() != null) {
                            p.teleport(p.getBedSpawnLocation());
                        }
                        else {
                            p.teleport(p.getWorld().getSpawnLocation());
                        }
                    }

                    p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 1));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 600, 2));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 600, 4));
                    p.setFoodLevel(20);
                    p.sendMessage(ChatColor.DARK_RED + "The Minecraft Gods have bestowed upon you an additional life due to all that you have accomplished. Don't waste it.");
                    p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1.0f);
                    p.playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1, 0.5f);

                    p.getServer().getOnlinePlayers().forEach(x -> {
                        if (!x.getName().equalsIgnoreCase(p.getName())) {
                            x.sendMessage(String.format("%s%s has been given an additional chance at life.", ChatColor.RED, p.getName()));
                        }
                    });

                    DatabaseOperations.updatePlayerLives(p, -1);
                }
            }
        }
    }
}
