package me.zombie_striker.civviecore.data;

import org.bukkit.Location;

public class CropBlock extends CivBlock{

    private CivBlock below;
    private long plantTime;
    private long growTime;

    public CropBlock(CivChunk chunk, CivBlock below, Location location, long plantTime, long growTime) {
        super(chunk, location);
        this.below = below;
        this.plantTime = plantTime;
        this.growTime = growTime;
    }


    public long getGrowTime() {
        return growTime;
    }

    public CivBlock getBelow() {
        if(below==null)
            below = getChunk().getBlockAt(getLocation().subtract(0,1,0));
        return below;
    }

    public long getPlantTime() {
        return plantTime;
    }

    @Override
    public int getReinforcement() {
        if(below==null)
            below = getChunk().getBlockAt(getLocation().subtract(0,1,0));
        if(below==null)
            return -1;
        return below.getReinforcement();
    }

    @Override
    public int getMaxReinforcement() {
        if(below==null)
            below = getChunk().getBlockAt(getLocation().subtract(0,1,0));
        if(below==null)
            return -1;
        return below.getMaxReinforcement();
    }

    public void setGrowTime(long growthFor) {
        this.growTime=growthFor;
    }

    public void setPlantTime(long currentTimeMillis) {
        this.plantTime = currentTimeMillis;
    }
}
