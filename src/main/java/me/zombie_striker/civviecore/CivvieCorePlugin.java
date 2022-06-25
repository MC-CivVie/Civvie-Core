package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.commands.ReinforceCommand;
import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import me.zombie_striker.civviecore.util.InternalFileUtil;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import me.zombie_striker.ezinventory.EZGUI;
import me.zombie_striker.ezinventory.EZInventoryCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public final class CivvieCorePlugin extends JavaPlugin {

    @Override
    public void onEnable() {

        new CivCore(this).init();

        try {
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "materials"),InternalFileUtil.getPathsToInternalFiles("materials"));
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "factories"),InternalFileUtil.getPathsToInternalFiles("factories"));
            InternalFileUtil.copyFilesOut(new File(getDataFolder(), "recipes"),InternalFileUtil.getPathsToInternalFiles("recipes"));
            InternalFileUtil.copyFilesOut(getDataFolder(),InternalFileUtil.getPathsToInternalFiles("basedir"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        EZInventoryCore.init(this);
        OreDiscoverUtil.init();

        ReinforceCommand rc = new ReinforceCommand(this);
        getCommand("reinforce").setExecutor(rc);
        getCommand("reinforce").setTabCompleter(rc);

        Bukkit.getPluginManager().registerEvents(new CivvieListener(this),this);


        new BukkitRunnable(){
            @Override
            public void run() {
                CivCore.getInstance().getFactoryManager().tick();
            }
        }.runTaskTimer(this,10,10);
        new BukkitRunnable(){
            @Override
            public void run() {
                for(CivWorld cw : CivCore.getInstance().getWorlds()){
                    for(CivChunk cc : cw.getChunks()){
                        cc.updateCrops();
                    }
                }
            }
        }.runTaskTimer(this,20*60,20*60);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
