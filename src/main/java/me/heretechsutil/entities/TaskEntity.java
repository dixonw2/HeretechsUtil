package me.heretechsutil.entities;

public class TaskEntity {
    private int id;
    private String taskDescription;
    private String difficulty;
    private int pointReward;
    private boolean completed;

    public TaskEntity(int id, String taskDescription, String difficulty, int pointReward, boolean completed) {
        this.id = id;
        this.taskDescription = taskDescription;
        this.difficulty = difficulty;
        this.pointReward = pointReward;
        this.completed = completed;
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

    public boolean getCompleted() { return completed; }

    public void setCompleted(boolean completed) { this.completed = completed; }
}
