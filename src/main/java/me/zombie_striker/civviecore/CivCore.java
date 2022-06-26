package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import me.zombie_striker.civviecore.managers.*;
import org.bukkit.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivCore {

    private static CivCore inst;
    private final CivvieCorePlugin plugin;

    private final FactoryManager factoryManager;
    private final ItemManager itemManager;
    private final GrowthManager growthManager;
    private final PearlManager pearlManager;
    private final PlayerStateManager playerStateManager;
    private final List<CivWorld> civworlds = new LinkedList<>();
    private final List<NameLayer> validNameLayers = new LinkedList<>();

    private HashMap<Material, Integer> reinforcelevel = new HashMap<>();

    public CivCore(CivvieCorePlugin plugin){
        inst = this;
        this.plugin = plugin;
        factoryManager = new FactoryManager(plugin);
        itemManager = new ItemManager();
        this.growthManager = new GrowthManager(plugin);
        this.pearlManager = new PearlManager(plugin);
        this.playerStateManager = new PlayerStateManager();
        reinforcelevel.put(Material.STONE,20);
        reinforcelevel.put(Material.COPPER_INGOT,50);
        reinforcelevel.put(Material.IRON_INGOT,200);
        reinforcelevel.put(Material.GOLD_INGOT,1000);
        reinforcelevel.put(Material.DIAMOND,1800);
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
            civChunk.updateCrops();
        }


        World prison = Bukkit.createWorld(new WorldCreator("prison").environment(World.Environment.NETHER));
        CivWorld prisonworld = new CivWorld(prison);
        civworlds.add(prisonworld);
        for(Chunk chunk : prison.getLoadedChunks()){
            CivChunk civChunk = CivChunk.load(chunk.getX(),chunk.getZ(), prisonworld);
            civChunk.updateCrops();
        }
    }

    public GrowthManager getGrowthManager() {
        return growthManager;
    }

    public PearlManager getPearlManager() {
        return pearlManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public CivvieCorePlugin getPlugin() {
        return plugin;
    }

    public List<NameLayer> getValidNameLayers() {
        return validNameLayers;
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public NameLayer getNameLayerCalled(UUID player, String name){
        for(NameLayer nl : validNameLayers){
            if(nl.getName().equals(name)){
                for(QuickPlayerData qpd : nl.getRanks().keySet()){
                    if(qpd.getUuid().equals(player))
                        return nl;
                }
            }
        }
        return null;
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

    public FactoryManager getFactoryManager() {
        return factoryManager;
    }

    public HashMap<Material, Integer> getReinforcelevel() {
        return reinforcelevel;
    }


    public List<CivWorld> getWorlds() {
        return civworlds;
    }

    public void registerNameLayer(NameLayer nameLayer){
        this.validNameLayers.add(nameLayer);
    }
    public void removeNameLayer(NameLayer nameLayer){
        this.validNameLayers.remove(nameLayer);
    }

}
