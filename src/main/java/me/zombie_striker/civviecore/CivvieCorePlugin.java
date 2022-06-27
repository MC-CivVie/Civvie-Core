package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.commands.CreateNameLayerCommand;
import me.zombie_striker.civviecore.commands.FactoryModCommand;
import me.zombie_striker.civviecore.commands.NameLayerCommand;
import me.zombie_striker.civviecore.commands.ReinforceCommand;
import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.util.InternalFileUtil;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import me.zombie_striker.ezinventory.EZInventoryCore;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public final class CivvieCorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        new CivvieAPI(this);
        try {
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "materials"),InternalFileUtil.getPathsToInternalFiles("materials"));
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "factories"),InternalFileUtil.getPathsToInternalFiles("factories"));
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "recipes"),InternalFileUtil.getPathsToInternalFiles("recipes"));
            InternalFileUtil.copyFilesOut(getDataFolder(),InternalFileUtil.getPathsToInternalFiles("basedir"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        Bukkit.getPluginManager().registerEvents(new CivvieListener(this),this);


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
                    for(CivChunk cc : cw.getChunks()){
                        cc.updateCrops();
                    }
                }
            }
        }.runTaskTimer(this,20*60,20*60);

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
                ConfigurationSection ranks =c.getConfigurationSection("namelayers."+key+".ranks");
                for(String key2 : ranks.getKeys(false)){
                    if(c.contains("namelayers."+key+".ranks."+key2+".rank")) {
                        NameLayerRankEnum rank = NameLayerRankEnum.valueOf(c.getString("namelayers." + key + ".ranks." + key2 + ".rank"));
                        nameLayer.getRanks().put(QuickPlayerData.getPlayerData(UUID.fromString(key2)), rank);
                    }
                }
                CivvieAPI.getInstance().registerNameLayer(nameLayer);
            }
        }

    }

    @Override
    public void onDisable() {

        for(CivWorld cv : CivvieAPI.getInstance().getWorlds()) {
            for (Chunk chunk : cv.getWorld().getLoadedChunks()){
                cv.getChunkAt(chunk.getX(), chunk.getZ()).unload();
            }
        }

        File namelayer = new File(getDataFolder(),"namelayers.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(namelayer);
        for(NameLayer nameLayer : CivvieAPI.getInstance().getValidNameLayers()){
            for(Map.Entry<QuickPlayerData, NameLayerRankEnum> e : nameLayer.getRanks().entrySet()) {
                c.set("namelayers."+nameLayer.getName() + ".ranks."+e.getKey().getUuid().toString()+".rank",e.getValue().name());
            }
        }
        try {
            c.save(namelayer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CivvieAPI.getInstance().getPearlManager().save();
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
