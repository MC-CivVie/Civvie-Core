package me.zombie_striker.ezinventory;

import org.bukkit.entity.Player;

public interface InventoryCallable {

    void onClick(Player clicker, int slot, boolean isShiftClick, boolean isRightClick);
}
