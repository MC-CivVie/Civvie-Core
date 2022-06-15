package me.zombie_striker.civviecore.data;

import org.bukkit.Bukkit;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class QuickPlayerData {

    private static List<QuickPlayerData> data = new LinkedList<>();
    private UUID uuid;
    private String lastName;

    private QuickPlayerData(String name, UUID uuid) {
        this.uuid = uuid;
        this.lastName = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLastName() {
        return lastName;
    }

    public static QuickPlayerData getPlayerData(UUID uuid){
        for(QuickPlayerData q : data){
            if(q.getUuid().equals(uuid))
                return q;
        }
        QuickPlayerData qqq = new QuickPlayerData(Bukkit.getOfflinePlayer(uuid).getName(), uuid);
        data.add(qqq);
        return qqq;
    }
}
