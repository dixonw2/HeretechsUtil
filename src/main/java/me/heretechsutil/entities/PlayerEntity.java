package me.heretechsutil.entities;

import java.util.List;

public class PlayerEntity {
    private int id;
    private String UUID;
    private String playerName;
    private double points;
    private List<TaskEntity> tasks;

    private int lives;

    public PlayerEntity(int id, String UUID, String playerName, int points, List<TaskEntity> tasks, int lives) {
        this.id = id;
        this.UUID = UUID;
        this.playerName = playerName;
        this.points = points;
        this.tasks = tasks;
        this.lives = lives;
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

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskEntity> tasks) {
        this.tasks = tasks;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}
