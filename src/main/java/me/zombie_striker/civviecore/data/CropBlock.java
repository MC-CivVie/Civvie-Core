package me.zombie_striker.civviecore.data;

import org.bukkit.Location;

public class CropBlock extends CivBlock{

    private CivBlock below;

    public CropBlock(CivChunk chunk, CivBlock below, Location location) {
        super(chunk, location);
        this.below = below;
    }

    @Override
    public int getReinforcement() {
        if(below==null)
            return -1;
        return below.getReinforcement();
    }

    @Override
    public int getMaxReinforcement() {
        if(below==null)
            return -1;
        return below.getMaxReinforcement();
    }
}
