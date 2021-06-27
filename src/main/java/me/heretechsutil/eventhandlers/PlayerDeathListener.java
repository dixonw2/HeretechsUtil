package me.heretechsutil.eventhandlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getEntity().getWorld().getPlayers().forEach(p -> {
            if (!p.isDead()) {
                p.setHealth(0);
                event.setDeathMessage(p.getName() + " was killed because " + event.getEntity().getName() + " died.");
            }
        });
    }
}
