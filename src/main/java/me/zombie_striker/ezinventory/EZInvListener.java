package me.zombie_striker.ezinventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EZInvListener implements Listener {

    private List<EZInventory> inventories = new LinkedList<>();

    @EventHandler
    public void onClick(InventoryClickEvent event){
        for(EZInventory ez : new ArrayList<>(inventories)){
            if(event.getClickedInventory()!=null)
            if(event.getClickedInventory().equals(ez.getInventory())){
                if(ez instanceof EZGUI){
                    event.setCancelled(true);
                    InventoryCallable ic = ((EZGUI) ez).getCallables()[event.getSlot()];
                    if(ic!=null){
                        ic.onClick((Player) event.getWhoClicked(),event.getSlot(),event.isShiftClick(),event.isRightClick());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        for(EZInventory ez : new ArrayList<>(inventories)){
            if(ez.getInventory().equals(event.getInventory())){
                if(ez.onClose()){
                    inventories.remove(ez);
                }
            }
        }
    }
    public void addEZInventory(EZInventory ez){
        this.inventories.add(ez);
    }
}
