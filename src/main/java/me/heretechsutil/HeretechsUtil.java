package me.heretechsutil;

import me.heretechsutil.commandexecutors.*;
import me.heretechsutil.eventhandlers.MobTargetListener;
import me.heretechsutil.eventhandlers.PlayerDamagedListener;
import me.heretechsutil.eventhandlers.PlayerJoinListener;
import me.heretechsutil.operations.ConfigOperations;
import me.heretechsutil.operations.DatabaseOperations;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public final class HeretechsUtil extends JavaPlugin {

    private static HeretechsUtil instance;

    @Override
    public void onEnable(){
        // Plugin startup logic
        instance = this;
        getServer().getPluginManager().registerEvents(new MobTargetListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamagedListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getCommand("points").setExecutor(new PointsCommandExecutor());
        getCommand("points").setTabCompleter(new TabCompletePointsCommandExecutor());
        getCommand("task").setExecutor(new TaskCommandExecutor());
        getCommand("task").setTabCompleter(new TabCompleteTaskCommandExecutor());
        getCommand("buy").setExecutor(new BuyCommandExecutor());
        getCommand("buy").setTabCompleter(new TabCompleteBuyCommandExecutor());

        World currentWorld = this.getServer().getWorld(getCurrentWorld());
        createFiles();

        DatabaseOperations.createTablesIfNotExist();
        DatabaseOperations.createNewWorldIfNotExists(currentWorld);
        DatabaseOperations.createTasks();
        DatabaseOperations.loadPlayers();
        ConfigOperations.loadBuyables();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static HeretechsUtil getInstance() {
        return instance;
    }

    private void createFiles() {
        File configf = new File(getDataFolder(), "config.yml");
        File createTables = new File(getDataFolder(), "CreateInitialTables.sql");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        saveResource("CreateInitialTables.sql", true);
        saveResource("CreateTasks.sql", true);
        FileConfiguration config = new YamlConfiguration();
        try {
            config.load(configf);
        }
        catch (InvalidConfigurationException | IOException e) {
            getLogger().info(e.getMessage());
        }
    }

    private String getCurrentWorld() {
        Properties pr = new Properties();
        try {
            FileInputStream in = new FileInputStream(new File("server.properties"));
            pr.load(in);
            return pr.getProperty("level-name");
        }
        catch (IOException e) {
            getLogger().log(Level.SEVERE, "Exception occurred when retrieving server.properties", e);
        }
        return "";
    }
}
