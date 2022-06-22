package me.zombie_striker.ezinventory;

import org.bukkit.plugin.java.JavaPlugin;

public class EZInventoryCore {

    private static boolean enabled = false;
    private static EZInventoryCore core;

    public static EZInventoryCore getInstance() {
        return core;
    }

    public static void init(JavaPlugin plugin) {
        if (enabled)
            return;
        enabled = true;
        core = new EZInventoryCore(plugin);
    }

    private EZInventoryCore(JavaPlugin plugin){

    }

}
