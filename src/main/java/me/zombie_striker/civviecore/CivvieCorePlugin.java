package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.commands.*;
import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.managers.PlayerStateManager;
import me.zombie_striker.civviecore.util.InternalFileUtil;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import me.zombie_striker.ezinventory.EZInventoryCore;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class CivvieCorePlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        int version = 1;
        if(!new File(getDataFolder(),"config.yml").exists() || !getConfig().contains("version") || getConfig().getInt("version") < version) {
            try {
                InternalFileUtil.copyFilesOut(new File(getDataFolder(), "materials"), InternalFileUtil.getPathsToInternalFiles("materials"), false);
                InternalFileUtil.copyFilesOut(new File(getDataFolder(), "factories"), InternalFileUtil.getPathsToInternalFiles("factories"), false);
                InternalFileUtil.copyFilesOut(new File(getDataFolder(), "recipes"), InternalFileUtil.getPathsToInternalFiles("recipes"), false);
                InternalFileUtil.copyFilesOut(getDataFolder(), InternalFileUtil.getPathsToInternalFiles("basedir"), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            getConfig().set("version",version);
            saveConfig();
        }
    }

    @Override
    public void onEnable() {
        new CivvieAPI(this);
        CivvieAPI.getInstance().init();


        EZInventoryCore.init(this);
        OreDiscoverUtil.init();

        ReinforceCommand rc = new ReinforceCommand();
        getCommand("reinforce").setExecutor(rc);
        getCommand("reinforce").setTabCompleter(rc);

        NameLayerCommand nlc = new NameLayerCommand();
        getCommand("nl").setExecutor(nlc);
        getCommand("nl").setTabCompleter(nlc);

        CreateNameLayerCommand cnlc = new CreateNameLayerCommand();
        getCommand("nlc").setExecutor(cnlc);
        getCommand("nlc").setTabCompleter(cnlc);

        FactoryModCommand fmc = new FactoryModCommand();
        getCommand("fm").setExecutor(fmc);
        getCommand("fm").setTabCompleter(fmc);

        NameLayerAcceptCommand nla = new NameLayerAcceptCommand();
        getCommand("nlaccept").setExecutor(nla);
        getCommand("nlaccept").setTabCompleter(nla);

        GlobalCommand gc = new GlobalCommand();
        getCommand("g").setTabCompleter(gc);
        getCommand("g").setExecutor(gc);

        Bukkit.getPluginManager().registerEvents(new CivvieListener(this),this);

        for(Iterator iterator = Bukkit.recipeIterator();iterator.hasNext();){
            Recipe recipe = (Recipe) iterator.next();
            if(recipe.getResult().getType()==Material.ENDER_EYE)
                iterator.remove();
            if(recipe.getResult().getType()==Material.ENDER_CHEST)
                iterator.remove();
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                CivvieAPI.getInstance().getFactoryManager().tick();
            }
        }.runTaskTimer(this,10,10);
        new BukkitRunnable(){
            @Override
            public void run() {
                for(CivWorld cw : CivvieAPI.getInstance().getWorlds()){
                    for(CivChunk cc : new LinkedList<>(cw.getChunks())){
                        cc.updateCrops();
                    }
                }
            }
        }.runTaskTimer(this,20*60,20*60);

        new BukkitRunnable(){
            @Override
            public void run() {
                for(Player player: Bukkit.getOnlinePlayers()) {
                    double distanceFromSpawn = player.getLocation().distanceSquared(new Location(player.getWorld(),0,player.getLocation().getY(),0));
                    if(distanceFromSpawn > 10000*10000){
                        if(distanceFromSpawn > 10025*10025){
                            player.damage(1);
                        }else{
                            player.sendMessage("Too close to the world boarder. Turn around!");
                        }
                    }
                }
            }
        }.runTaskTimer(this,20,20);

        new BukkitRunnable(){
            public void run(){
                CivvieAPI.getInstance().getTickManager().tick();
            }
        }.runTaskTimer(this,1,1);


        File namelayer = new File(getDataFolder(),"namelayers.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(namelayer);
        if(c.contains("namelayers")){
            for(String key : c.getConfigurationSection("namelayers").getKeys(false)){
                NameLayer nameLayer = new NameLayer(key);
                if(c.contains("namelayers."+key+".ranks")) {
                    ConfigurationSection ranks = c.getConfigurationSection("namelayers." + key + ".ranks");
                    for (String key2 : ranks.getKeys(false)) {
                        if (c.contains("namelayers." + key + ".ranks." + key2 + ".rank")) {
                            NameLayerRankEnum rank = NameLayerRankEnum.valueOf(c.getString("namelayers." + key + ".ranks." + key2 + ".rank"));
                            nameLayer.getRanks().put(QuickPlayerData.getPlayerData(UUID.fromString(key2)), rank);
                        }
                    }
                }

                if(c.contains("namelayers."+key+".perms")) {
                    for (String key3 : c.getConfigurationSection("namelayers." + key + ".perms").getKeys(false)) {
                        boolean value = c.getBoolean("namelayers." + key + ".perms." + key3);
                        NameLayer.NameLayerPermissions perm = NameLayer.NameLayerPermissions.valueOf(key3);
                        nameLayer.getPermissionsMap().put(perm, value);
                    }
                }
                CivvieAPI.getInstance().registerNameLayer(nameLayer);
            }
        }
        File playerstatefile = new File(getDataFolder(),"playerstate.yml");
        FileConfiguration c3 = YamlConfiguration.loadConfiguration(playerstatefile);
        if(c3.contains("chat")){
            for(String uuidstring : c3.getConfigurationSection("chat").getKeys(false)){
                UUID uuid = UUID.fromString(uuidstring);
                UUID namelayeruuid = UUID.fromString(c3.getString("chat."+uuidstring+".nl"));
                NameLayer nameLayer = CivvieAPI.getInstance().getNameLayer(namelayeruuid);
                if(nameLayer!=null){
                    CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(new PlayerStateManager.NameLayerChatState(uuid,nameLayer));
                }
            }
        }

    }

    @Override
    public void onDisable() {

        for(CivWorld cv : CivvieAPI.getInstance().getWorlds()) {
            for (Chunk chunk : cv.getWorld().getLoadedChunks()){
                cv.getChunkAt(chunk.getX(), chunk.getZ()).unload();
            }
            cv.unload();
        }

        File namelayer = new File(getDataFolder(),"namelayers.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(namelayer);
        for(NameLayer nameLayer : CivvieAPI.getInstance().getValidNameLayers()){
            for(Map.Entry<QuickPlayerData, NameLayerRankEnum> e : nameLayer.getRanks().entrySet()) {
                c.set("namelayers."+nameLayer.getName() + ".ranks."+e.getKey().getUuid().toString()+".rank",e.getValue().name());
            }
            for(Map.Entry<NameLayer.NameLayerPermissions, Boolean> e : nameLayer.getPermissionsMap().entrySet()){
                c.set("namelayers."+nameLayer.getName()+".perms."+e.getKey(),e.getValue());
            }
        }
        try {
            c.save(namelayer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CivvieAPI.getInstance().getPearlManager().save();

        File file = new File(getDataFolder(),"combatlogger.yml");
        FileConfiguration c2 = YamlConfiguration.loadConfiguration(file);
        List<String> uuids = new LinkedList<>();
        for(UUID uuid:CivvieAPI.getInstance().getCombatLogManager().getPlayersKilledOffline()){
            uuids.add(uuid.toString());
        }
        c2.set("list",uuids);
        try {
            c2.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        File playerstatefile = new File(getDataFolder(),"playerstate.yml");
        FileConfiguration c3 = YamlConfiguration.loadConfiguration(playerstatefile);
        for(PlayerStateManager.PlayerState state : CivvieAPI.getInstance().getPlayerStateManager().getPlayerstates()){
            if(state instanceof PlayerStateManager.NameLayerChatState){
                c3.set("chat."+state.getUuid().toString()+".nl",((PlayerStateManager.NameLayerChatState) state).getNameLayer().getNlUUID().toString());
            }
        }
        try {
            c3.save(playerstatefile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public File getWorldData(String worldname){
        File f = new File(getDataFolder(),"worlddata/"+worldname);
        if(!f.exists())
            f.mkdirs();
        return f;
    }
    public File getChunkData(int x, int z , String world){
        File c = new File(getWorldData(world),x+"_"+z+".yml");
        if(!c.exists()){
            try {
                c.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return c;
    }
}
