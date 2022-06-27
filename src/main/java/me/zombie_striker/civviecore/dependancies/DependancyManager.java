package me.zombie_striker.civviecore.dependancies;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Bukkit;

public class DependancyManager {

    private TerraManager terraManager;

    public DependancyManager(CivvieCorePlugin core){
        if(Bukkit.getPluginManager().isPluginEnabled("Terra")){
            terraManager=new TerraManager(core);
        }
    }

    public boolean hasTerra(){
        return terraManager!=null;
    }

    public TerraManager getTerraManager() {
        return terraManager;
    }
}
