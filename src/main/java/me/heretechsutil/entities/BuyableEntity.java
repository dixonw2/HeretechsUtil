package me.heretechsutil.entities;

import org.bukkit.Material;

public class BuyableEntity {
    private String item;
    private double cost;

    public BuyableEntity(String item, double cost) {
        this.item = item;
        this.cost = cost;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
