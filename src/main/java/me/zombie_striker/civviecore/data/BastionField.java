package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivvieAPI;
import org.bukkit.Location;

public class BastionField {

    private final Location bastionBlock;
    private final int radius;
    private NameLayer nameLayer;

    public BastionField(Location bastionBlock, int radius, NameLayer nameLayer){
        this.bastionBlock=bastionBlock;
        this.radius=radius;
        this.nameLayer = nameLayer;
    }

    public NameLayer getNameLayer() {
        if(bastionBlock.isChunkLoaded()){
            return nameLayer = CivvieAPI.getInstance().getWorld(bastionBlock.getWorld().getName()).getChunkAt(bastionBlock.getChunk().getX(),bastionBlock.getChunk().getZ()).getBlockAt(bastionBlock).getOwner();
        }
        return nameLayer;
    }

    public int getRadius() {
        return radius;
    }

    public Location getBastionBlock() {
        return bastionBlock;
    }
}
