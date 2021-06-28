package me.heretechsutil.eventhandlers;

import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        DatabaseOperations.createNewPlayerIfNotExists(p);
        // TODO: assign tasks

    }
}
