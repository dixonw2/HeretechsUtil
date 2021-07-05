package me.heretechsutil.entities;

import java.util.ArrayList;
import java.util.List;

public class PlayerEntity {
    private int id;
    private String UUID;
    private String playerName;
    private int points;
    private List<TaskEntity> tasks = new ArrayList<>();

    public PlayerEntity(int id, String UUID, String playerName, int points, List<TaskEntity> tasks) {
        this.id = id;
        this.UUID = UUID;
        this.playerName = playerName;
        this.points = points;
        this.tasks = tasks;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
    }
}
