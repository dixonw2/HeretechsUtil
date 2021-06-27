package me.heretechsutil.eventhandlers;

import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

import static org.bukkit.Bukkit.getLogger;

public class MobTargetListener implements Listener {

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        Entity attacker = event.getEntity();
        Entity target = event.getTarget();

        if (target instanceof Player && attacker instanceof Monster) {
            Player p = (Player) target;
            // 20 ticks a second, 3600 seconds an hour
            long timeInHours = (p.getStatistic(Statistic.TOTAL_WORLD_TIME) / 20) / 3600;
            double multiplier = timeInHours / 240.0;
            //double multiplier = 25;
            if (multiplier > 1) {
                Random ran = new Random();
                if (ran.nextInt(10) == 0) {
                    Monster mob = (Monster) attacker;
                    if (!(mob instanceof Creeper) || !(mob instanceof Skeleton))
                        mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(mob.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getValue() * multiplier);

                    if (mob instanceof Zombie && multiplier >= 2)
                        mob.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).setBaseValue(mob.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS).getValue() * 6);

                    if (mob instanceof Skeleton && multiplier >= 2)
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3));

                    if (mob instanceof Spider && multiplier >= 2) {
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 4));
                        mob.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
                    }
                }
            }
        }
    }
}
