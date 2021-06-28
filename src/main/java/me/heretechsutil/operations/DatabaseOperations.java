package me.heretechsutil.operations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.heretechsutil.HeretechsUtil;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DatabaseOperations {

    private static final HeretechsUtil util = HeretechsUtil.getInstance();
    private static final DataSource dataSource = createDataSource();

    public static boolean playerExists(Player p) {
        String methodTrace = "DatabaseOperations.playerExists():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT id FROM Player P WHERE P.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next())
                return true;
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while checking if player %s exists",
                        methodTrace, p.getName()), e);
        }
        return false;
    }

    public static boolean worldExists(World w) {
        String methodTrace = "DatabaseOperations.worldExists():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT W.id FROM World W WHERE W.UUID = ?")) {
            cmd.setString(1, w.getUID().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while checking if world %s exists",
                        methodTrace, w.getName()), e);
        }
        return false;
    }

    public static boolean playerWorldExists(Player p, World w) {
        String methodTrace = "DatabaseOperations.playerWorldExists():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
        "SELECT PW.id FROM PlayerWorld PW " +
            "JOIN Player P ON P.id = PW.idPlayer " +
            "JOIN World W ON W.id = PW.idWorld " +
            "WHERE P.UUID = ? AND W.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            cmd.setString(2, w.getUID().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while checking if world %s exists",
                        methodTrace, w.getName()), e);
        }
        return false;
    }

    public static void createNewPlayerIfNotExists(Player p) {
        String methodTrace = "DatabaseOperations.createNewPlayerIfNotExists():";
        if (!playerExists(p)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO Player (UUID, PlayerName, Points) " +
                            "VALUES (?, ?, ?)")) {
                cmd.setString(1, p.getUniqueId().toString());
                cmd.setString(2, p.getName());
                cmd.setInt(3, 0);
                cmd.executeUpdate();
                util.getLogger().info(String.format("%s Player %s created", methodTrace, p.getName()));
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                        String.format("%s Exception occurred while creating player %s",
                            methodTrace, p.getName()), e);
            }
        }
        if (!playerWorldExists(p, p.getWorld()))
            createPlayerWorldIfNotExists(p, p.getWorld());
    }

    public static void createNewWorldIfNotExists(World w) {
        String methodTrace = "DatabaseOperations.createNewWorldIfNotExists():";
        if (!worldExists(w)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO World (WorldName, UUID, Active) " +
                        "VALUES (?, ?, ?)")) {
                cmd.setString(1, w.getName());
                cmd.setString(2, w.getUID().toString());
                cmd.setBoolean(3, true);
                cmd.executeUpdate();

                util.getLogger().info(String.format("%s World %s created", methodTrace, w.getName()));
                setInactiveWorlds(w);
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, String.format("%s Exception occurred while creating world %s",
                    methodTrace, w.getName()), e);
            }
        }
    }

    public static void createPlayerWorldIfNotExists(Player p, World w) {
        String methodTrace = "DatabaseOperations.createPlayerWorldIfNotExists(): ";
        if (!playerWorldExists(p, w)) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO PlayerWorld (idPlayer, idWorld) " +
                            "SELECT P.id, W.id " +
                            "FROM Player P " +
                            "JOIN World W ON W.UUID = ? AND W.Active = ? " +
                            "WHERE P.UUID = ?")) {
                cmd.setString(1, w.getUID().toString());
                cmd.setBoolean(2, true);
                cmd.setString(3, p.getUniqueId().toString());
                cmd.executeUpdate();

                util.getLogger().info(String.format("%s PlayerWorld created for player %s, world %s",
                        methodTrace, p.getName(), w.getName()));
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while creating PlayerWorld for player %s, world %s",
                            methodTrace, p.getName(), w.getName()), e);
            }
        }
    }

    public static void setInactiveWorlds(World w) {
        String methodTrace = "DatabaseOperations.setInactiveWorlds():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE World SET Active = ? WHERE UUID <> ? AND Active = ?")) {
            cmd.setBoolean(1, false);
            cmd.setString(2, w.getUID().toString());
            cmd.setBoolean(3, true);
            cmd.executeUpdate();
            util.getLogger().info(String.format("%s Active worlds set to inactive", methodTrace));
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                String.format("%s Exception occurred while updating inactive worlds", methodTrace), e);
        }
    }

    public static int getPointsForPlayer(Player p) {
        String methodTrace = "DatabaseOperations.getPointsForPlayer():";
        int points = 0;
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT P.Points FROM Player P WHERE P.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            ResultSet rs = cmd.executeQuery();

            if (rs.next()) {
                points = rs.getInt("Points");
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE, String.format("%s Exception occurred while getting player %s's points",
                methodTrace, p.getName()), e);
        }
        return points;
    }

    // add a positive or negative number to the player's points
    public static void addPointsToPlayer(Player p, int points) {
        String methodTrace = "DatabaseOperations.addPointsToPlayer():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE Player p SET p.Points = p.Points + ? WHERE p.UUID = ?")) {
            cmd.setInt(1, points);
            cmd.setString(2, p.getUniqueId().toString());
            cmd.executeUpdate();
            util.getLogger().info(String.format("%s Updated %s's points",
                    methodTrace, p.getName()));
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE, String.format("%s Exception occurred while %s %d points %s %s",
                    methodTrace, points >= 0 ? "adding" : "subtracting", points,
                    points >= 0 ? "to" : "from", p.getName()), e);
        }
    }

    public static void createTablesIfNotExist() {
        String setup = "";
        try (InputStream in = new FileInputStream("plugins/HeretechsUtil/CreateInitialTables.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            util.getLogger().log(Level.SEVERE, "Could not read CreateInitialTables setup files", e);
        }

        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isEmpty()) continue;
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(query)) {
                cmd.execute();
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, "Exception occurred while creating tables", e);
            }
        }
    }

    private static DataSource createDataSource() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "com.mysql.cj.jdbc.MysqlConnectionPoolDataSource");
        props.setProperty("dataSource.serverName", util.getConfig().getString("database.host"));
        props.setProperty("dataSource.portNumber", util.getConfig().getString("database.port"));
        props.setProperty("dataSource.databaseName", util.getConfig().getString("database.database-name"));
        props.setProperty("dataSource.user", util.getConfig().getString("database.user"));
        props.setProperty("dataSource.password", util.getConfig().getString("database.password"));

        HikariConfig config = new HikariConfig(props);
        return new HikariDataSource(config);
    }
}
