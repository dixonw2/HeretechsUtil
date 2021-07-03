package me.heretechsutil.entities;

public class PlayerWorldEntity {
    private int idPlayerWorld;
    private int idPlayer;
    private int idWorld;

    public PlayerWorldEntity(int idPlayerWorld, int idPlayer, int idWorld) {
        this.idPlayerWorld = idPlayerWorld;
        this.idPlayer = idPlayer;
        this.idWorld = idWorld;
    }

    public int getIdPlayerWorld() {
        return idPlayerWorld;
    }

    public void setIdPlayerWorld(int idPlayerWorld) {
        this.idPlayerWorld = idPlayerWorld;
    }

    public int getIdPlayer() {
        return idPlayer;
    }

    public void setIdPlayer(int idPlayer) {
        this.idPlayer = idPlayer;
    }

    public int getIdWorld() {
        return idWorld;
    }

    public void setIdWorld(int idWorld) {
        this.idWorld = idWorld;
    }
}
