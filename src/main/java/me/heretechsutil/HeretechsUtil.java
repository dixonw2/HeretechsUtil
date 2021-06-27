package me.heretechsutil;

import me.heretechsutil.eventhandlers.CreatureSpawnListener;
import me.heretechsutil.eventhandlers.MobTargetListener;
import me.heretechsutil.eventhandlers.PlayerDeathListener;
import me.heretechsutil.eventhandlers.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeretechsUtil extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new MobTargetListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // TODO: create tables if not exists
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

    }
}
