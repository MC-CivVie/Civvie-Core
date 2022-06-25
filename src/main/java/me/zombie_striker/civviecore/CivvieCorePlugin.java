package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.commands.ReinforceCommand;
import me.zombie_striker.civviecore.util.InternalFileUtil;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        OreDiscoverUtil.init();

        ReinforceCommand rc = new ReinforceCommand(this);
        getCommand("reinforce").setExecutor(rc);
        getCommand("reinforce").setTabCompleter(rc);

        Bukkit.getPluginManager().registerEvents(new CivvieListener(this),this);


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
