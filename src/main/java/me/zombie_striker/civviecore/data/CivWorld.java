package me.zombie_striker.civviecore.data;

import org.bukkit.World;

import java.util.LinkedList;
import java.util.List;

public class CivWorld {

    private List<CivChunk> chunks = new LinkedList<>();

    private World world;

    public CivWorld(World world){
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public List<CivChunk> getChunks() {
        return chunks;
    }
    public CivChunk getChunkAt(int x, int z){
        for(CivChunk cc : chunks){
            if(cc.getX() == x && cc.getZ() == z){
                return cc;
            }
        }
        return null;
    }
}
