package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.managers.FactoryManager;
import org.bukkit.Location;
import org.bukkit.block.data.Ageable;
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

    private List<CropBlock> cropBlocks = new LinkedList<>();
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
            for(String key : c.getConfigurationSection("factory").getKeys(false)) {
                String[] split = key.split("\\,");
                Location craftingTable = civchunk.stringToLocation(split[0]);
                Location furnace = civchunk.stringToLocation(split[1]);
                Location chest = civchunk.stringToLocation(split[2]);

                String factoryType = c.getString("factory."+key+".type");
                FactoryManager.FactoryType ft = CivCore.getInstance().getFactoryManager().getFactoryTypeByName(factoryType);

                String recipe = c.getString("factory."+key+".recipe");
                FactoryRecipe factoryRecipe = CivCore.getInstance().getFactoryManager().getRecipeByName(recipe);

                boolean running = c.getBoolean("factory."+key+".running");
                if(ft != null) {
                    FactoryBuild fb = new FactoryBuild(craftingTable, furnace, chest,ft);
                    fb.setCurrentRecipe(factoryRecipe);
                    fb.setRunning(running);
                    civchunk.factories.add(fb);
                }
            }
        }
        if(c.contains("crops")){
            for(String key : c.getConfigurationSection("crops").getKeys(false)){
                Location croploc = civchunk.stringToLocation(key);

                long plant = c.getLong("crops."+key+".planted");
                long growth = c.getLong("crops."+key+".growtime");

                CropBlock cropBlock = new CropBlock(civchunk,civchunk.getBlockAt(croploc.clone().subtract(0,1,0)),croploc,plant,growth);
                civchunk.addCivBlock(cropBlock);
                civchunk.cropBlocks.add(cropBlock);
            }
        }
        return civchunk;
    }

    public void updateCrops(){
        for(CropBlock cropBlock : cropBlocks){
            long growStageTime = System.currentTimeMillis()- cropBlock.getPlantTime();
            double stage = growStageTime/ cropBlock.getGrowTime();
            if(cropBlock.getLocation().getBlock().getBlockData() instanceof Ageable){
                Ageable age = (Ageable) cropBlock.getLocation().getBlock().getBlockData();
                int stageAge = (int) Math.min(age.getMaximumAge(),stage* age.getMaximumAge());
                if(stageAge!=age.getAge()) {
                    age.setAge(stageAge);
                    cropBlock.getLocation().getBlock().setBlockData(age);
                }
            }
        }
    }

    public List<CropBlock> getCropBlocks() {
        return cropBlocks;
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
            c.set("factory."+sb.toString()+".type",fb.getType().getName());
            c.set("factory."+sb.toString()+".recipe",fb.getCurrentRecipe().getName());
            c.set("factory."+sb.toString()+".running",fb.isRunning());
        }
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
