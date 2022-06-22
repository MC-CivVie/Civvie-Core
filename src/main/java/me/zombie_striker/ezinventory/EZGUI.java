package me.zombie_striker.ezinventory;

import org.bukkit.inventory.Inventory;

public class EZGUI extends EZInventory{

    private InventoryCallable[] callables;


    public EZGUI(Inventory inventory) {
        super(inventory);
        callables = new InventoryCallable[inventory.getSize()];
    }

    public InventoryCallable[] getCallables() {
        return callables;
    }
}
