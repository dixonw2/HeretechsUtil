package me.heretechsutil.operations;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import me.heretechsutil.HeretechsUtil;
import me.heretechsutil.entities.PlayerEntity;
import me.heretechsutil.entities.PlayerWorldEntity;
import me.heretechsutil.entities.TaskEntity;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DatabaseOperations {

    private static final HeretechsUtil util = HeretechsUtil.getInstance();
    private static final DataSource dataSource = createDataSource();
    private static final Map<String, PlayerEntity> players = new HashMap<>();

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
                            "JOIN PlayerWorldTask PWT ON PWT.idPlayerWorld = ?")) {
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

    public static List<PlayerEntity> getPlayers() {
        return new ArrayList<>(players.values());
    }

    public static PlayerEntity getPlayer(Player p) {
        return players.get(p.getUniqueId().toString());
    }

    // Create a new player on server join if they have not been on before with 20 points and 0 lives by default
    public static void createNewPlayerIfNotExists(Player p) {
        String methodTrace = "DatabaseOperations.createNewPlayerIfNotExists():";
        if (!playerExists(p)) {
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement cmd = conn.prepareStatement(
                    "INSERT INTO Player (UUID, PlayerName, Points) " +
                        "VALUES (?, ?, ?)");
                cmd.setString(1, p.getUniqueId().toString());
                cmd.setString(2, p.getName());
                cmd.setInt(3, 20);
                cmd.executeUpdate();
                util.getLogger().info(String.format("%s Player %s created", methodTrace, p.getName()));

                cmd = conn.prepareStatement("SELECT id FROM Player WHERE UUID = ?");
                cmd.setString(1, p.getUniqueId().toString());
                ResultSet rs = cmd.executeQuery();
                if (rs.next()) {
                    players.put(p.getUniqueId().toString(),
                        new PlayerEntity(rs.getInt("id"), p.getUniqueId().toString(), p.getName(), 20, new ArrayList<>(), 0));
                }
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                        String.format("%s Exception occurred while creating player %s",
                            methodTrace, p.getName()), e);
            }
        }
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

    // sets other worlds to inactive if their names do not match the current world name specified in the server.properties file
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

    public static void updatePlayerLives(Player p, int life) {
        String methodTrace = "DatabaseOperations.updatePlayerLives():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE Player p SET p.Lives = p.Lives + ? WHERE UUID = ?")) {
            cmd.setInt(1, life);
            cmd.setString(2, p.getUniqueId().toString());
            cmd.executeUpdate();
            PlayerEntity player = getPlayer(p);
            player.setLives(player.getLives() + life);
            util.getLogger().info(String.format("%s Updated %s's lives",
                    methodTrace, p.getName()));
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while updating %s's lives", methodTrace, p.getName()), e);
        }
    }

    public static double getPointsForPlayer(Player p) {
        return players.get(p.getUniqueId().toString()).getPoints();
    }

    // get a specific task based on the player and the task's description
    public static TaskEntity getTaskForPlayer(Player p, String description) {
        List<TaskEntity> list = players.get(p.getUniqueId().toString()).getTasks().stream().
                filter(x -> x.getTaskDescription().equalsIgnoreCase(description)).collect(Collectors.toList());
        if (list.size() != 1) {
            return null;
        }
        return list.get(0);
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

    public static List<TaskEntity> getTasksForPlayer(Player p, String difficulty) {
        return players.get(p.getUniqueId().toString()).getTasks().stream().
            filter(x -> difficulty.equalsIgnoreCase("all") ||
            x.getDifficulty().equalsIgnoreCase(difficulty)).collect(Collectors.toList());
    }

    public static void addPointsToPlayer(Player p, double points) {
        String methodTrace = "DatabaseOperations.addPointsToPlayer():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE Player p SET p.Points = p.Points + ? WHERE p.UUID = ?")) {
            cmd.setDouble(1, points);
            cmd.setString(2, p.getUniqueId().toString());
            cmd.executeUpdate();
            PlayerEntity pe = players.get(p.getUniqueId().toString());
            pe.setPoints(pe.getPoints() + points);
            util.getLogger().info(String.format("%s Updated %s's points",
                    methodTrace, p.getName()));
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE, String.format("%s Exception occurred while %s %.2f points %s %s",
                    methodTrace, points >= 0 ? "adding" : "subtracting", points,
                    points >= 0 ? "to" : "from", p.getName()), e);
        }
    }

    // redeem a non-completed task for a player
    public static void redeemTask(Player p, TaskEntity task) {
        String methodTrace = "DatabaseOperations.redeemTask():";
        PlayerWorldEntity pw = getPlayerWorldEntityForActiveWorld(p);
        if (pw != null && !players.get(p.getUniqueId().toString()).getTasks().stream().
                filter(x -> x.getTaskDescription().equalsIgnoreCase(task.getTaskDescription())).findFirst().get().getCompleted()) {
            try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "UPDATE PlayerWorldTask PWT " +
                    "JOIN Task T ON T.id = PWT.idTask " +
                    "SET Completed = true " +
                    "WHERE PWT.idPlayerWorld = ? " +
                    "AND T.TaskDescription = ? " +
                    "AND PWT.Completed <> true")) {
                cmd.setInt(1, pw.getIdPlayerWorld());
                cmd.setString(2, task.getTaskDescription());
                cmd.executeUpdate();

                addPointsToPlayer(p, task.getPointReward());
                players.get(p.getUniqueId().toString()).getTasks().stream().
                    filter(x -> x.getTaskDescription().equalsIgnoreCase(task.getTaskDescription())).findFirst().get().setCompleted(true);
                util.getLogger().info(String.format("%s %s redeemed task [%s] and received %d points",
                    methodTrace, p.getName(), task.getTaskDescription(), task.getPointReward()));
            }
            catch (SQLException e) {
                util.getLogger().log(Level.SEVERE,
                    String.format("%s Exception occurred while redeeming task %s for player %s",
                    methodTrace, task.getTaskDescription(), p.getName()), e);
            }
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
            if (players.containsKey(p.getUniqueId().toString()) && players.get(p.getUniqueId().toString()).getTasks().isEmpty()) {
                PlayerWorldEntity pw = getPlayerWorldEntityForActiveWorld(p);
                if (pw != null) {
                    try (Connection conn = dataSource.getConnection()) {
                        PreparedStatement cmd = conn.prepareStatement(
                                "SELECT T.id, T.TaskDescription, T.Difficulty, T.PointReward " +
                                        "FROM Task T " +
                                        "JOIN World W ON W.Active " +
                                        "JOIN PlayerWorld PW ON PW.idWorld = W.id " +
                                        "JOIN Player P ON P.id = PW.idPlayer AND P.UUID = ? " +
                                        "LEFT JOIN PlayerWorldTask PWT ON PWT.idTask = T.id AND PWT.idPlayerWorld = PW.id " +
                                        "WHERE PWT.id IS NULL"
                        );
                        cmd.setString(1, p.getUniqueId().toString());
                        List<TaskEntity> tasks = new ArrayList<>();
                        ResultSet rs = cmd.executeQuery();
                        while (rs.next()) {
                            tasks.add(new TaskEntity(rs.getInt("id"), rs.getString("TaskDescription"),
                                    rs.getString("Difficulty"), rs.getInt("PointReward"), false));
                        }
                        List<TaskEntity> progression = tasks.stream().filter(x -> x.getDifficulty().
                            equalsIgnoreCase("progression")).collect(Collectors.toList());
                        List<TaskEntity> easy = tasks.stream().filter(x -> x.getDifficulty().
                            equalsIgnoreCase("easy")).collect(Collectors.toList());
                        List<TaskEntity> medium = tasks.stream().filter(x -> x.getDifficulty().
                            equalsIgnoreCase("medium")).collect(Collectors.toList());
                        List<TaskEntity> hard = tasks.stream().filter(x -> x.getDifficulty().
                            equalsIgnoreCase("hard")).collect(Collectors.toList());
                        List<TaskEntity> playerTasks = new ArrayList<>();

                        Random r = new Random();
                        for (int i = 0; i < 30; i++) {
                            if (i >= 27) {
                                int index = r.nextInt(hard.size());
                                playerTasks.add(hard.get(index));
                                hard.remove(index);
                            }
                            else if (i >= 22) {
                                int index = r.nextInt(medium.size());
                                playerTasks.add(medium.get(index));
                                medium.remove(index);
                            }
                            else if (i >= 15) {
                                int index = r.nextInt(easy.size());
                                playerTasks.add(easy.get(index));
                                easy.remove(index);
                            }
                            else {
                                int index = r.nextInt(progression.size());
                                playerTasks.add(progression.get(index));
                                progression.remove(index);
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

                        players.get(p.getUniqueId().toString()).setTasks(playerTasks);
                    }
                    catch (SQLException e) {
                        util.getLogger().log(Level.SEVERE,
                                String.format("%s Exception occurred while creating PlayerWorldTasks for player %s",
                                        methodTrace, p.getName()), e);
                    }
                }
            }

        }
    }

    // load players on startup and load into a HashMap so get operations don't rely on touching the database
    public static void loadPlayers() {
        String methodTrace = "DatabaseOperations.loadPlayers():";
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
            "SELECT P.id, P.UUID, P.PlayerName, P.Points, P.Lives " +
                "FROM Player P")) {
            ResultSet rs = cmd.executeQuery();
            while (rs.next()) {
                PlayerEntity pe = new PlayerEntity(rs.getInt("id"), rs.getString("UUID"),
                    rs.getString("PlayerName"),
                    rs.getInt("Points"), loadTasksForPlayer(rs.getInt("id")), rs.getInt("Lives"));
                players.put(rs.getString("UUID"), pe);
                util.getLogger().info(String.format("Loaded player %s with %.2f points and %d %s",
                    pe.getPlayerName(), pe.getPoints(), pe.getLives(), pe.getLives() == 1 ? "life" : "lives"));
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE, String.format("%s Exception occurred while loading players", methodTrace));
        }
    }

    private static List<TaskEntity> loadTasksForPlayer(int playerId) {
        String methodTrace = "DatabaseOperations.loadTasksForPlayer():";
        List<TaskEntity> tasks = new ArrayList<>();
        try (Connection conn = dataSource.getConnection(); PreparedStatement cmd = conn.prepareStatement(
                "SELECT T.id, T.TaskDescription, T.Difficulty, T.PointReward, PWT.Completed " +
                "FROM PlayerWorld PW " +
                "JOIN World W ON W.id = PW.idWorld AND W.Active " +
                "JOIN PlayerWorldTask PWT ON PWT.idPlayerWorld = PW.id " +
                "JOIN Task T ON T.id = PWT.idTask " +
                "WHERE PW.idPlayer = ? " +
                "ORDER BY FIELD(T.Difficulty, 'Progression', 'Easy', 'Medium', 'Hard')")) {
            cmd.setInt(1, playerId);
            ResultSet rs = cmd.executeQuery();

            while (rs.next()) {
                tasks.add(new TaskEntity(rs.getInt("id"), rs.getString("TaskDescription"),
                    rs.getString("Difficulty"), rs.getInt("PointReward"), rs.getBoolean("Completed")));
            }
        }
        catch (SQLException e) {
            util.getLogger().log(Level.SEVERE,
                String.format("%s Exception occurred while loading tasks for player with id %d", methodTrace, playerId));
        }
        return tasks;
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
