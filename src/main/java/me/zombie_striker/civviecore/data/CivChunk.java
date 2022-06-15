package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivChunk {

    private List<CivBlock> civBlocks = new LinkedList<>();
    private int x;
    private int z;
    private CivWorld world;
    public CivChunk(int x, int z,  CivWorld world){
        this.x=x;this.z=z;this.world=world;
    }

    public static CivChunk load(int x, int z, CivWorld world) {
        CivChunk civchunk = new CivChunk(x,z,world);
        File config = CivCore.getInstance().getPlugin().getChunkData(x,z,world.getWorld().getName());

        FileConfiguration c = YamlConfiguration.loadConfiguration(config);
        if(c.contains("blocks")){
            for(String key : c.getConfigurationSection("blocks").getKeys(false)){
                String[] parts = key.split("\\_");
                int xb = Integer.parseInt(parts[0]);
                int yb = Integer.parseInt(parts[1]);
                int zb = Integer.parseInt(parts[2]);

                int reinforce = c.getInt("blocks."+key+".r");
                int maxreinforce = c .getInt("blocks."+key+".mr");
                NameLayer layer = CivCore.getInstance().getNameLayer(UUID.fromString(c.getString("blocks."+key+".uuid")));

                CivBlock block = new CivBlock(civchunk, new Location( world.getWorld(),xb,yb,zb));

                block.setOwner(layer);
                block.setMaxReinforcement(maxreinforce);
                block.setReinforcement(reinforce);


                civchunk.civBlocks.add(block);
            }
        }
        return civchunk;
    }

    public CivBlock getBlockAt(Location location){
        for(CivBlock cb : civBlocks){
            if(cb.getLocation().equals(location))
                return cb;
        }
        return null;
    }

    public List<CivBlock> getCivBlocks() {
        return civBlocks;
    }

    public void addCivBlock(CivBlock civBlock){
        this.civBlocks.add(civBlock);
    }

    public CivWorld getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
