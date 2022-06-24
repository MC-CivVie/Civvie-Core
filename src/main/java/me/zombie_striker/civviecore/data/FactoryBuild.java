package me.zombie_striker.civviecore.data;

import org.bukkit.Location;

public class FactoryBuild {

    private Location craftingTable;
    private Location furnace;
    private Location chest;


    public FactoryBuild(Location craftingTable, Location furnace, Location chest){
        this.craftingTable = craftingTable;
        this.furnace = furnace;
        this.chest = chest;
    }

    public Location[] getBlockLocations(){
        return new Location[]{craftingTable,furnace,chest};
    }

    public Location getChest() {
        return chest;
    }

    public Location getCraftingTable() {
        return craftingTable;
    }

    public Location getFurnace() {
        return furnace;
    }
}
