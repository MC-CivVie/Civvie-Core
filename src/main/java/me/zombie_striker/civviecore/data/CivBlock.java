package me.zombie_striker.civviecore.data;

import org.bukkit.Location;
import org.bukkit.Material;

public class CivBlock {

    private Location location;
    private CivChunk chunk;

    private NameLayer owner;
    private int reinforcement;
    private int maxReinforcement;
    private Material reinforcedWith;

    public CivBlock(CivChunk chunk, Location location){
        this.location = location;
        this.chunk = chunk;
    }

    public Location getLocation() {
        return location;
    }

    public CivChunk getChunk() {
        return chunk;
    }

    public int getMaxReinforcement() {
        return maxReinforcement;
    }

    public int getReinforcement() {
        return reinforcement;
    }

    public NameLayer getOwner() {
        return owner;
    }

    public void setOwner(NameLayer owner) {
        this.owner = owner;
    }

    public void setReinforcedWith(Material reinforcedWith) {
        this.reinforcedWith = reinforcedWith;
    }

    public Material getReinforcedWith() {
        return reinforcedWith;
    }

    public void setReinforcement(int reinforcement) {
        this.reinforcement = reinforcement;
    }

    public void setMaxReinforcement(int maxReinforcement) {
        this.maxReinforcement = maxReinforcement;
    }
}
