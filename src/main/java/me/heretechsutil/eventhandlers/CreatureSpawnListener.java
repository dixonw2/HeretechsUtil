package me.heretechsutil.eventhandlers;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity e = event.getEntity();
        if (e instanceof Monster) {

        }
    }
}
