package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PearlManager {

    private List<PearlData> pearls = new LinkedList<>();
    private CivvieCorePlugin plugin;
    private char[] CODENAME = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','W','R','S','T','U','V','W','X','Y','Z'};

    public boolean isPearled(OfflinePlayer player){
        for(PearlData pd : pearls){
            if(pd.getUuid().equals(player.getUniqueId()))
                return true;
        }
        return false;
    }

    public PearlManager(CivvieCorePlugin core){
        this.plugin = core;

        File co = new File(plugin.getDataFolder(),"pearls.yml");
        if(!co.exists()) {
            try {
                co.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(co);
        if(c.contains("pearls")){
            for(String codename : c.getConfigurationSection("pearls").getKeys(false)){
                UUID uuid = UUID.fromString(c.getString("pearls."+codename));
                PearlData pd = new PearlData(uuid, codename);
                pearls.add(pd);
            }
        }
    }

    public void save(){
        File co = new File(plugin.getDataFolder(),"pearls.yml");
        if(!co.exists()) {
            try {
                co.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(co);
        for(PearlData pearlData : pearls){
            c.set("pearls."+pearlData.getDesignation(),pearlData.getUuid().toString());
        }
        try {
            c.save(co);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String createPearl(OfflinePlayer player){
        StringBuilder codename = new StringBuilder();
        for(int i = 0; i < 8; i++){
            codename.append(CODENAME[ThreadLocalRandom.current().nextInt(i)]);
        }

        PearlData pearlData = new PearlData(player.getUniqueId(),codename.toString());
        pearls.add(pearlData);
        return codename.toString();
    }

    public class PearlData{
        private UUID uuid;
        private String designation;

        public PearlData(UUID uuid, String des){
            this.uuid = uuid;
            this.designation = des;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getDesignation() {
            return designation;
        }
    }
}
