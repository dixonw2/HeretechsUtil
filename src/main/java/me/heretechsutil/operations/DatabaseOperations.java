package me.heretechsutil.operations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.heretechsutil.HeretechsUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class DatabaseOperations {

    private static final HeretechsUtil util = HeretechsUtil.getInstance();
    private static final DataSource dataSource = createDataSource();

   /* public static void createTables() {
        String setup = "";
        try (InputStream in = new FileInputStream("..\\..\\..\\resources\\CreateInitialTables.sql")) {
            setup = new BufferedReader(
                    new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));

        }
        catch (IOException e) {
            util.getLogger().log(Level.SEVERE, "Could not read CreateInitialTables file", e);
        }
        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isEmpty())
                continue;
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(query)) {
                cmd.execute();
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, "Exception occurred when creating the tables", e);
            }
        }
    }*/

    public static boolean playerExists(Player p) {
        String methodTrace = "DatabaseOperations.playerExists(): ";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT id FROM Player p WHERE p.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next())
                return true;
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    methodTrace + "Exception occurred while checking if player " + p.getName() + " exists", e);
        }
        return false;
    }

    public static boolean worldExists(World w) {
        String methodTrace = "DatabaseOperations.worldExists(): ";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT w.id FROM World w WHERE w.UUID = ?")) {
            cmd.setString(1, w.getUID().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    methodTrace + "Exception occurred while checking if world " + w.getName() + " exists", e);
        }
        return false;
    }

    public static boolean playerWorldExists(Player p, World w) {
        String methodTrace = "DatabaseOperations.playerWorldExists(): ";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
        "SELECT pw.id FROM PlayerWorld pw " +
            "JOIN Player p ON p.id = pw.idPlayer " +
            "JOIN World w ON w.id = pw.idWorld " +
            "WHERE p.UUID = ? AND w.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            cmd.setString(2, w.getUID().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    methodTrace + "Exception occurred while checking if world " + w.getName() + " exists", e);
        }
        return false;
    }

    public static void createNewPlayerIfNotExists(Player p) {
        String methodTrace = "DatabaseOperations.createNewPlayerIfNotExists(): ";
        util.getLogger().info(playerExists(p) ? "I exist" : "I don't exist?");
        if (!playerExists(p)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO Player (UUID, PlayerName, Points) " +
                            "VALUES (?, ?, ?)")) {
                cmd.setString(1, p.getUniqueId().toString());
                cmd.setString(2, p.getName());
                cmd.setInt(3, 0);
                cmd.executeUpdate();
                util.getLogger().info(methodTrace + "player " + p.getName() + " created");
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                        methodTrace + "Exception occurred while creating player " + p.getName(), e);
            }
        }
        if (!playerWorldExists(p, p.getWorld()))
            createPlayerWorldIfNotExists(p, p.getWorld());
    }

    public static void createNewWorldIfNotExists(World w) {
        String methodTrace = "DatabaseOperations.createNewWorldIfNotExists(): ";
        util.getLogger().info(worldExists(w) ? "I exist world" + w.getName() : "I don't exist?" + w.getName());
        if (!worldExists(w)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO World (WorldName, UUID, Active) " +
                        "VALUES (?, ?, ?)")) {
                cmd.setString(1, w.getName());
                cmd.setString(2, w.getUID().toString());
                cmd.setBoolean(3, true);
                cmd.executeUpdate();

                util.getLogger().info(methodTrace + "world " + w.getName() + " created");
                setInactiveWorlds(w);
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, methodTrace + "Exception occurred while creating world " + w.getName(), e);
            }
        }
    }

    public static void createPlayerWorldIfNotExists(Player p, World w) {
        String methodTrace = "DatabaseOperations.createPlayerWorldIfNotExists(): ";
        if (!playerWorldExists(p, w)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO PlayerWorld (idPlayer, idWorld) " +
                            "SELECT p.id, w.id " +
                            "FROM Player p " +
                            "JOIN World w ON w.UUID = ? AND w.Active = ? " +
                            "WHERE p.UUID = ?")) {
                cmd.setString(1, w.getUID().toString());
                cmd.setBoolean(2, true);
                cmd.setString(3, p.getUniqueId().toString());
                cmd.executeUpdate();

                util.getLogger().info(methodTrace + "playerworld created for player " + p.getName() +
                        ", world " + w.getName());
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, methodTrace + "Exception occurred while creating playerworld for " +
                        "player " + p.getName() + ", world " + w.getName(), e);
            }
        }
    }

    public static void setInactiveWorlds(World w) {
        String methodTrace = "DatabaseOperations.setInactiveWorlds(): ";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE World SET Active = ? WHERE UUID <> ? AND Active = ?")) {
            cmd.setBoolean(1, false);
            cmd.setString(2, w.getUID().toString());
            cmd.setBoolean(3, true);
            cmd.executeUpdate();
            util.getLogger().info(methodTrace + "active worlds set to inactive");
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE, methodTrace + "Exception occurred while updating old worlds", e);
        }
    }

    private static DataSource createDataSource() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "com.mysql.cj.jdbc.MysqlConnectionPoolDataSource");
        props.setProperty("dataSource.serverName", util.getConfig().getString("database.host"));
        props.setProperty("dataSource.portNumber", util.getConfig().getString("database.port"));
        props.setProperty("dataSource.databaseName", util.getConfig().getString("database.databaseName"));
        props.setProperty("dataSource.user", util.getConfig().getString("database.user"));
        props.setProperty("dataSource.password", util.getConfig().getString("database.password"));

        HikariConfig config = new HikariConfig(props);
        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }
}
