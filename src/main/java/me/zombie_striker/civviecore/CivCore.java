package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import me.zombie_striker.civviecore.data.NameLayer;
import org.bukkit.*;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivCore {

    private static CivCore inst;
    private CivvieCorePlugin plugin;
    private List<CivWorld> civworlds = new LinkedList<>();
    private List<NameLayer> validNameLayers = new LinkedList<>();

    public CivCore(CivvieCorePlugin plugin){
        inst = this;
        this.plugin = plugin;
    }

    public static CivCore getInstance(){
        return inst;
    }

    public void playReinforceProtection(Location location) {
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0,0.5,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0.5,0,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(1,0.5,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0.5,1,0.5), 1,-1,0,1);
    }

    public void init(){
        World starterworld = Bukkit.getWorlds().get(0);
        CivWorld civworld = new CivWorld(starterworld);
        civworlds.add(civworld);

        for(Chunk chunk : starterworld.getLoadedChunks()){
            CivChunk civChunk = CivChunk.load(chunk.getX(),chunk.getZ(), civworld);
        }
    }

    public CivvieCorePlugin getPlugin() {
        return plugin;
    }

    public List<NameLayer> getValidNameLayers() {
        return validNameLayers;
    }

    public NameLayer getNameLayer(UUID uuid){
        for(NameLayer nl : validNameLayers){
            if(nl.getNlUUID().equals(uuid))
                return nl;
        }
        return null;
    }

    public CivWorld getWorld(String name){
        for(CivWorld worlds : civworlds){
            if(worlds.getWorld().getName().equals(name))
                return worlds;
        }
        return null;
    }
}
