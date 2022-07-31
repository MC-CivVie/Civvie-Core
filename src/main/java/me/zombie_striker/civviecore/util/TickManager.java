package me.zombie_striker.civviecore.util;

import org.bukkit.Material;

public class TickManager {

    private long lastTick=System.currentTimeMillis();

    public TickManager(){
    }

    public void tick(){
        lastTick = System.currentTimeMillis();
    }

    public long getLastTick() {
        return lastTick;
    }
}
