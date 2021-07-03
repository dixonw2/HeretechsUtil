package me.heretechsutil.operations;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.PlayerWorldEntity;
import me.heretechsutil.entities.PlayerWorldTaskEntity;
import me.heretechsutil.entities.TaskEntity;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
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

    public static boolean playerWorldExists(Player p) {
        return getPlayerWorldEntityForActiveWorld(p) != null;
    }

    public static boolean playerWorldTaskExists(Player p) {
        String methodTrace = "DatabaseOperations.playerWorldTaskExists():";
        PlayerWorldEntity pw = getPlayerWorldEntityForActiveWorld(p);
        if (pw != null) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                    "SELECT PW.id FROM PlayerWorld PW " +
                            "JOIN PlayerWorldTask PWT ON PWT.idPlayerWorld = PW.id " +
                            "WHERE PW.id = ?")) {
                cmd.setInt(1, pw.getIdPlayerWorld());
                ResultSet rs = cmd.executeQuery();
                if (rs.next()) {
                    return true;
                }
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                        String.format("%s Exception occurred while checking if active world exists for player %s",
                                methodTrace, p.getName()), e);
            }
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
                cmd.setInt(3, 20);
                cmd.executeUpdate();
                util.getLogger().info(String.format("%s Player %s created", methodTrace, p.getName()));
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                        String.format("%s Exception occurred while creating player %s",
                            methodTrace, p.getName()), e);
            }
        }
        if (!playerWorldExists(p))
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
        if (!playerWorldExists(p)) {
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

    public static PlayerWorldEntity getPlayerWorldEntityForActiveWorld(Player p) {
        String methodTrace = "DatabaseOperations.getPlayerWorldEntityForActiveWorld():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT WP.id idWP, P.id idP, W.id idW " +
                        "FROM Player P " +
                        "JOIN PlayerWorld WP ON WP.idPlayer = P.id " +
                        "JOIN World W ON W.id = WP.idWorld " +
                        "WHERE W.Active " +
                        "AND P.UUID = ?")) {
            cmd.setString(1, p.getUniqueId().toString());
            ResultSet rs = cmd.executeQuery();
            if (rs.next())
                return new PlayerWorldEntity(rs.getInt("idWP"),
                        rs.getInt("idP"), rs.getInt("idW"));
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while assigning tasks to player %s",
                            methodTrace, p.getName()));
        }
        return null;
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
                return;
            }
        }
    }

    public static void createTasks() {
        String setup = "";
        try (InputStream in = new FileInputStream("plugins/HeretechsUtil/CreateTasks.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
        }
        catch (IOException e) {
            util.getLogger().log(Level.SEVERE, "Could not read CreateTasks setup files", e);
        }

        if (!setup.isEmpty()) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(setup)) {
                cmd.executeUpdate();
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE, "Exception occurred while creating tasks", e);
            }
        }
    }

    public static void createPlayerWorldTasks(Player p) {
        if (!playerWorldTaskExists(p)) {
            String methodTrace = "DatabaseOperations.createPlayerWorldTasks():";
            PlayerWorldEntity pw = getPlayerWorldEntityForActiveWorld(p);
            if (pw != null) {
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement cmd = conn.prepareStatement(
                            "SELECT T.id, T.TaskDescription, T.Difficulty, T.PointReward " +
                                    "FROM Task T " +
                                    "JOIN World W ON W.Active " +
                                    "JOIN PlayerWorld PW ON PW.idWorld = W.id " +
                                    "LEFT JOIN Player P ON P.id = PW.idPlayer " +
                                    "LEFT JOIN PlayerWorldTask PWT ON PWT.idTask = T.id " +
                                    "WHERE PWT.id IS NULL"
                    );
                    List<TaskEntity> tasks = new ArrayList<>();
                    ResultSet rs = cmd.executeQuery();
                    while (rs.next()) {
                        tasks.add(new TaskEntity(rs.getInt("id"), rs.getString("TaskDescription"),
                                rs.getString("Difficulty"), rs.getInt("PointReward")));
                    }
                    List<TaskEntity> easy = tasks.stream().filter(x -> x.getDifficulty()
                            .equalsIgnoreCase("Easy")).collect(Collectors.toList());
                    List<TaskEntity> medium = tasks.stream().filter(x -> x.getDifficulty()
                            .equalsIgnoreCase("Medium")).collect(Collectors.toList());
                    List<TaskEntity> hard = tasks.stream().filter(x -> x.getDifficulty()
                            .equalsIgnoreCase("Hard")).collect(Collectors.toList());
                    List<TaskEntity> playerTasks = new ArrayList<>();

                    Random r = new Random();
                    for (int i = 0; i < 10; i++) {
                        if (i >= 8) {
                            int index = r.nextInt(hard.size());
                            playerTasks.add(hard.get(index));
                            hard.remove(index);
                        }
                        else if (i >= 4) {
                            int index = r.nextInt(medium.size());
                            playerTasks.add(medium.get(index));
                            medium.remove(index);
                        }
                        else {
                            int index = r.nextInt(easy.size());
                            playerTasks.add(easy.get(index));
                            easy.remove(index);
                        }
                    }

                    cmd = conn.prepareStatement("INSERT INTO PlayerWorldTask (Completed, Assigned, idTask, idPlayerWorld)" +
                            " VALUES (?, ?, ?, ?)");
                    conn.setAutoCommit(false);
                    for (TaskEntity te : playerTasks) {
                        cmd.setBoolean(1, false);
                        cmd.setBoolean(2, true);
                        cmd.setInt(3, te.getId());
                        cmd.setInt(4, pw.getIdPlayerWorld());
                        cmd.addBatch();
                    }
                    cmd.executeBatch();
                    conn.commit();

                }
                catch (SQLException e) {
                    util.getLogger().log(Level.SEVERE,
                            String.format("%s Exception occurred while creating PlayerWorldTasks for player %s",
                                    methodTrace, p.getName()), e);
                }
            }
        }
    }

    private static DataSource createDataSource() {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setDatabaseName(util.getConfig().getString("database.database-name"));
        ds.setServerName(util.getConfig().getString("database.host"));
        ds.setPort(util.getConfig().getInt("database.port"));
        ds.setUser(util.getConfig().getString("database.user"));
        ds.setPassword(util.getConfig().getString("database.password"));
        return ds;
    }
}
