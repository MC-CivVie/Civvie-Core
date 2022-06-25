package me.zombie_striker.ezinventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class EZGUI extends EZInventory{

    private InventoryCallable[] callables;


    public EZGUI(Inventory inventory) {
        super(inventory);
        callables = new InventoryCallable[inventory.getSize()];
        EZInventoryCore.getInstance().getListener().addEZInventory(this);
    }

    @Override
    public boolean onClose() {
        return true;
    }

    public InventoryCallable[] getCallables() {
        return callables;
    }

    public void addCallable(ItemStack itemstack, InventoryCallable inventoryCallable, int slot){
        callables[slot] = inventoryCallable;
        getInventory().setItem(slot,itemstack);
    }
}
