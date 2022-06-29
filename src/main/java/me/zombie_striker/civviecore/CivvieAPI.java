package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import me.zombie_striker.civviecore.dependancies.DependancyManager;
import me.zombie_striker.civviecore.managers.*;
import me.zombie_striker.civviecore.util.TickManager;
import org.bukkit.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CivvieAPI {

    private static CivvieAPI inst;
    private final CivvieCorePlugin plugin;

    private final FactoryManager factoryManager;
    private final ItemManager itemManager;
    private final GrowthManager growthManager;
    private final PearlManager pearlManager;
    private final PlayerStateManager playerStateManager;
    private final TickManager tickManager;
    private final DependancyManager dependancyManager;
    private final BossBarManager bossBarManager;
    private final CombatLogManager combatLogManager;
    private final List<CivWorld> civworlds = new LinkedList<>();
    private final List<NameLayer> validNameLayers = new LinkedList<>();

    private HashMap<Material, Integer> reinforcelevel = new HashMap<>();

    public CivvieAPI(CivvieCorePlugin plugin){
        inst = this;
        this.plugin = plugin;
        if(!plugin.getDataFolder().exists())
            plugin.getDataFolder().mkdirs();
        this.dependancyManager = new DependancyManager(plugin);
        this.tickManager = new TickManager();
        this.itemManager = new ItemManager(plugin);
        this.factoryManager = new FactoryManager(plugin);
        this.growthManager = new GrowthManager(plugin);
        this.pearlManager = new PearlManager(plugin);
        this.playerStateManager = new PlayerStateManager();
        this.bossBarManager = new BossBarManager();
        this.combatLogManager = new CombatLogManager(plugin);
        reinforcelevel.put(Material.STONE,20);
        reinforcelevel.put(Material.COPPER_INGOT,50);
        reinforcelevel.put(Material.IRON_INGOT,200);
        reinforcelevel.put(Material.GOLD_INGOT,1000);
        reinforcelevel.put(Material.DIAMOND,1800);
    }

    public static CivvieAPI getInstance(){
        return inst;
    }

    public void playReinforceProtection(Location location) {
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0,0.5,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0.5,0,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(1,0.5,0.5), 1,-1,0,1);
        location.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, location.clone().add(0.5,1,0.5), 1,-1,0,1);
    }

    public void init(){
        itemManager.init(getPlugin());
        factoryManager.init(getPlugin());
        for(World world : Bukkit.getWorlds()){
            CivWorld civworld = new CivWorld(world);
            civworlds.add(civworld);
            civworld.init();
            for(Chunk chunk : world.getLoadedChunks()){
                CivChunk civChunk = CivChunk.load(chunk.getX(), chunk.getZ(), civworld);
                civChunk.updateCrops();
            }
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

    public DependancyManager getDependancyManager() {
        return dependancyManager;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public CombatLogManager getCombatLogManager() {
        return combatLogManager;
    }

    public CivvieCorePlugin getPlugin() {
        return plugin;
    }

    public TickManager getTickManager() {
        return tickManager;
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
