package me.heretechsutil;

import me.heretechsutil.commandexecutors.PointsCommandExecutor;
import me.heretechsutil.commandexecutors.TabCompleteCommandExecutor;
import me.heretechsutil.eventhandlers.MobTargetListener;
import me.heretechsutil.eventhandlers.PlayerDeathListener;
import me.heretechsutil.eventhandlers.PlayerJoinListener;
import me.heretechsutil.operations.DatabaseOperations;
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

    private File configf;
    private FileConfiguration config;
    private static HeretechsUtil instance;

    @Override
    public void onEnable(){
        // Plugin startup logic
        instance = this;
        getServer().getPluginManager().registerEvents(new MobTargetListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getCommand("points").setExecutor(new PointsCommandExecutor());
        getCommand("points").setTabCompleter(new TabCompleteCommandExecutor());

        createFiles();
        DatabaseOperations.createNewWorldIfNotExists(this.getServer().getWorld(getCurrentWorld()));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static HeretechsUtil getInstance() {
        return instance;
    }

    private void createFiles() {
        configf = new File(getDataFolder(), "config.yml");

        if (!configf.exists()) {
            configf.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        config = new YamlConfiguration();
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
            String world = pr.getProperty("level-name");
            return world;
        }
        catch (IOException e) {
            getLogger().log(Level.SEVERE, "Exception occurred when retrieving server.properties", e);
        }
        return "";
    }
}
