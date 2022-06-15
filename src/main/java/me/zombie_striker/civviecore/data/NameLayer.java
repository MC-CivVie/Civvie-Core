package me.zombie_striker.civviecore.data;

import java.util.HashMap;
import java.util.UUID;

public class NameLayer {

    private HashMap<QuickPlayerData, NameLayerRankEnum> ranks = new HashMap<>();

    private String name;
    private UUID nlUUID;

    public NameLayer(String name){
        this.name = name;
        this.nlUUID = UUID.randomUUID();
    }
    public NameLayer(UUID uuid, String name){
        this.name = name;
        this.nlUUID = uuid;
    }

    public String getName() {
        return name;
    }

    public UUID getNlUUID() {
        return nlUUID;
    }

    public HashMap<QuickPlayerData, NameLayerRankEnum> getRanks() {
        return ranks;
    }
}
