package me.heretechsutil.entities;

public class PlayerWorldTaskEntity {
    private int id;
    private boolean completed;
    private boolean assigned;
    private int idTask;
    private int idPlayerWorld;

    public PlayerWorldTaskEntity(int id, boolean completed, boolean assigned, int idTask, int idPlayerWorld) {
        this.id = id;
        this.completed = completed;
        this.assigned = assigned;
        this.idTask = idTask;
        this.idPlayerWorld = idPlayerWorld;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public int getIdTask() {
        return idTask;
    }

    public void setIdTask(int idTask) {
        this.idTask = idTask;
    }

    public int getIdPlayerWorld() {
        return idPlayerWorld;
    }

    public void setIdPlayerWorld(int idPlayerWorld) {
        this.idPlayerWorld = idPlayerWorld;
    }
}
