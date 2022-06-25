package me.zombie_striker.ezinventory;

import org.bukkit.inventory.Inventory;

import javax.swing.text.LabelView;

public abstract class EZInventory {

    private final Inventory inventory;

    public EZInventory(Inventory inventory){
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Called when the inventory is closed by a player
     * @return whether to remove the inventory from the list.
     */
    public abstract boolean onClose();
}
