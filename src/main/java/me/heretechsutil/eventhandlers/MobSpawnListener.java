package me.heretechsutil.eventhandlers;

import me.heretechsutil.HeretechsUtil;
import org.bukkit.ChatColor;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

import static org.bukkit.Bukkit.getLogger;

public class MobSpawnListener implements Listener {

    private static final String[] names = new String[]{"Timmy", "Jerry", "Miranda", "Jacob", "Harry", "Wyatt",
        "Kristen", "Samuel", "Brandon", "Henry", "Logan", "Alexander", "Benjamin", "Joe", "Carly", "Sam", "Freddie",
        "Jackson", "Jimothy", "Michael", "Eleanor", "Chidi", "Jason", "Tahani", "Janet", "Papa John", "Billy Boi"};
    private static final String[] titles = new String[]{"Undesirable", "Unfair", "Hungry", "Cool", "Adequate",
        "Hotdog", "Depressed", "Mad", "Furry", "Messy", "Sloppy Joe", "Foot", "Time Knife", "Burrito", "Master",
        "Sick", "Alpha", "Hairy", "Wizard", "Moth", "Pizza Delivery Man", ChatColor.MAGIC + "Corrupted"};

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Monster) {
            double time = HeretechsUtil.getInstance().getConfig().getInt("multiplier-time");
            if (time > 0) {
                Monster mob = (Monster) event.getEntity();
                // 20 ticks a second, 3600 seconds an hour
                double timeInHours = 0;
                for (Player p : mob.getServer().getOnlinePlayers()) {
                    timeInHours += (p.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20.0) / 3600;
                }
                timeInHours /= mob.getServer().getOnlinePlayers().size();
                double multiplier = timeInHours / time;
                if (multiplier > 1) {
                    double maxMultiplier = HeretechsUtil.getInstance().getConfig().getDouble("max-multiplier");
                    if (multiplier > maxMultiplier && maxMultiplier != 0)
                        multiplier = maxMultiplier;

                    Random ran = new Random();
                    if (ran.nextInt(10) == 0) {
                        mob.setCustomName(String.format("%s the %s", names[ran.nextInt(names.length)], titles[ran.nextInt(titles.length)]));
                        if (!(mob instanceof Creeper) || !(mob instanceof AbstractSkeleton))
                            mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(
                                mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() * multiplier);

                        if (mob instanceof Zombie && multiplier >= 1.5)
                            mob.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(
                                mob.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).getValue() * 10);

                        if (mob instanceof Skeleton && multiplier >= 1.5)
                            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));

                        if (mob instanceof Spider && multiplier >= 1.5) {
                            mob.addPotionEffect(
                                new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 2));
                            mob.addPotionEffect(
                                new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
                        }
                    }
                }
            }
        }
    }
}
