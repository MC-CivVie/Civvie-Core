package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivvieAPI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.checkerframework.checker.units.qual.C;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivWorld {

    private final List<BastionField> bastionFields = new LinkedList<>();
    private List<CivChunk> chunks = new LinkedList<>();

    private World world;

    public CivWorld(World world) {
        this.world = world;
    }

    public void init(){
        File config = new File(CivvieAPI.getInstance().getPlugin().getWorldData(getWorld().getName()),"worlddata.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(config);
        if(c.contains("bastion")){
            for(String key : c.getConfigurationSection("bastion").getKeys(false)){
                Location loc = stringToLocation(key);
                int radius = c.getInt("bastion."+key+".r");
                NameLayer nl = CivvieAPI.getInstance().getNameLayer(UUID.fromString(c.getString("baston."+key+".nl")));

                BastionField bf = new BastionField(loc,radius,nl);
                bastionFields.add(bf);
            }
        }
    }

    public World getWorld() {
        return world;
    }

    public List<CivChunk> getChunks() {
        return chunks;
    }

    public void unload(){
        File config = new File(CivvieAPI.getInstance().getPlugin().getWorldData(getWorld().getName()),"worlddata.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(config);
        for(BastionField bf : bastionFields){
            c.set("bastion." + bf.getBastionBlock().getBlockX() + "_" + bf.getBastionBlock().getBlockY() + "_" + bf.getBastionBlock().getBlockZ() + ".r", bf.getRadius());
            c.set("bastion." + bf.getBastionBlock().getBlockX() + "_" + bf.getBastionBlock().getBlockY() + "_" + bf.getBastionBlock().getBlockZ() + ".nl", bf.getNameLayer().getNlUUID());
        }
        try {
            c.save(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Location stringToLocation(String location) {
        String[] split = location.split("\\_");

        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);
        return new Location(world, x, y, z);
    }
    public CivChunk getChunkAt(int x, int z) {
        for (CivChunk cc : chunks) {
            if (cc.getX() == x && cc.getZ() == z) {
                return cc;
            }
        }
        CivChunk civChunk = new CivChunk(x, z, this);
        chunks.add(civChunk);
        return civChunk;
    }

    public void addBastion(BastionField bb) {
        this.bastionFields.add(bb);
    }

    public void removeBastion(BastionField cblock) {
        this.bastionFields.remove(cblock);
    }

    public List<BastionField> getBastionFields() {
        return this.bastionFields;
    }
}
