package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FuelManager {

    private HashMap<UUID,Integer> fuel = new HashMap<>();

    public FuelManager(CivvieCorePlugin plugin){
        File file = new File(plugin.getDataFolder(),"fuelapples.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if(config.contains("fuel")){
            for(String uuidstring : config.getConfigurationSection("fuel").getKeys(false)){
                UUID uuid = UUID.fromString(uuidstring);
                int amount = config.getInt("fuel."+uuidstring+".amount");
                this.fuel.put(uuid,amount);
            }
        }
    }
    public void save(){
        File file = new File(CivvieAPI.getInstance().getPlugin().getDataFolder(),"fuelapples.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for(Map.Entry<UUID, Integer> e : fuel.entrySet()){
            config.set("fuel."+e.getKey().toString()+".amount",e.getValue());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HashMap<UUID, Integer> getFuel() {
        return fuel;
    }
    public void setFuel(UUID uuid, int amount){
        this.fuel.put(uuid,amount);
    }
    public int getFuel(UUID uuid){
        if(!fuel.containsKey(uuid)){
            fuel.put(uuid,0);
        }
        return fuel.get(uuid);
    }
}
