package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivCore;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivChunk {

    private List<CivBlock> civBlocks = new LinkedList<>();

    private List<FactoryBuild> factories = new LinkedList<>();
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
        if(c.contains("factory")){
            for(String key : c.getStringList("factory")) {
                String[] split = key.split("\\,");
                Location craftingTable = civchunk.stringToLocation(split[0]);
                Location furnace = civchunk.stringToLocation(split[1]);
                Location chest = civchunk.stringToLocation(split[2]);
                FactoryBuild fb = new FactoryBuild(craftingTable,furnace,chest);
                civchunk.factories.add(fb);
            }
        }
        return civchunk;
    }

    public void unload(){
        File config = CivCore.getInstance().getPlugin().getChunkData(x,z,world.getWorld().getName());
        if(!config.exists()){
            try {
                config.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(config);

        for(CivBlock cb : civBlocks){
            c.set("blocks."+cb.getLocation().getBlockX()+"_"+cb.getLocation().getBlockY()+"_"+cb.getLocation().getBlockZ()+".r",cb.getReinforcement());
            c.set("blocks."+cb.getLocation().getBlockX()+"_"+cb.getLocation().getBlockY()+"_"+cb.getLocation().getBlockZ()+".mr",cb.getMaxReinforcement());
            c.set("blocks."+cb.getLocation().getBlockX()+"_"+cb.getLocation().getBlockY()+"_"+cb.getLocation().getBlockZ()+".uuid",cb.getOwner().getNlUUID());
        }
        List<String> factor = new LinkedList<>();
        for(FactoryBuild fb : factories){
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getCraftingTable().getBlockX());
            sb.append("_");
            sb.append(fb.getCraftingTable().getBlockY());
            sb.append("_");
            sb.append(fb.getCraftingTable().getBlockZ());
            sb.append(",");
            sb.append(fb.getFurnace().getBlockX());
            sb.append("_");
            sb.append(fb.getFurnace().getBlockY());
            sb.append("_");
            sb.append(fb.getFurnace().getBlockZ());
            sb.append(",");
            sb.append(fb.getChest().getBlockX());
            sb.append("_");
            sb.append(fb.getChest().getBlockY());
            sb.append("_");
            sb.append(fb.getChest().getBlockZ());
            factor.add(sb.toString());
        }
        c.set("factory",factor);
        try {
            c.save(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void removeCivBlock(CivBlock civBlock){
        this.civBlocks.remove(civBlock);
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

    public List<FactoryBuild> getFactories() {
        return factories;
    }

    public Location stringToLocation(String location){
        String[] split = location.split("\\_");

        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);
        return new Location(world.getWorld(),x,y,z);
    }

    public void addFactory(FactoryBuild fb) {
        factories.add(fb);
    }
    public void removeFactory(FactoryBuild fb){
        factories.remove(fb);
    }
}
