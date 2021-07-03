package me.heretechsutil.entities;

public class TaskEntity {
    private int id;
    private String taskDescription;
    private String difficulty;
    private int pointReward;

    public TaskEntity(int id, String taskDescription, String difficulty, int pointReward) {
        this.id = id;
        this.taskDescription = taskDescription;
        this.difficulty = difficulty;
        this.pointReward = pointReward;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getPointReward() {
        return pointReward;
    }

    public void setPointReward(int pointReward) {
        this.pointReward = pointReward;
    }
}
