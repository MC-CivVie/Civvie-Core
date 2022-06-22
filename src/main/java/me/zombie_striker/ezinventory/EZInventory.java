package me.zombie_striker.ezinventory;

import org.bukkit.inventory.Inventory;

public class EZInventory {

    private final Inventory inventory;

    public EZInventory(Inventory inventory){
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
