package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class PearlManager {

    private List<PearlData> pearls = new LinkedList<>();
    private CivvieCorePlugin plugin;
    private char[] CODENAME = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'W', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public boolean isPearled(OfflinePlayer player) {
        for (PearlData pd : pearls) {
            if (pd.getUuid().equals(player.getUniqueId()))
                return true;
        }
        return false;
    }

    public PearlManager(CivvieCorePlugin core) {
        this.plugin = core;

        File co = new File(plugin.getDataFolder(), "pearls.yml");
        if (!co.exists()) {
            try {
                co.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(co);
        if (c.contains("pearls")) {
            for (String codename : c.getConfigurationSection("pearls").getKeys(false)) {
                UUID uuid = UUID.fromString(c.getString("pearls." + codename + ".uuid"));
                String killer = c.getString("pearls." + codename + ".killer");
                long time = c.getLong("pearls." + codename + ".time");
                long lasttime = c.getLong("pearls." + codename + ".lasttime");
                double fuel = c.getDouble("pearls." + codename + ".fuel");
                PearlData pd = new PearlData(uuid, codename, time, lasttime, killer, fuel);
                if (c.contains("pearls." + codename + ".holder")) {
                    Object result = c.get("pearls." + codename + ".holder");
                    if (result instanceof Location) {
                        PearlBlockHolder pbh = new PearlBlockHolder(pd, (Location) result);
                        pd.setPearlHolder(pbh);
                    } else if (result instanceof String) {
                        UUID entityuuid = UUID.fromString((String) result);
                        Entity entity = Bukkit.getEntity(entityuuid);
                        if (entity != null) {
                            PearlEntityHolder peh = new PearlEntityHolder(pd, entity);
                            pd.setPearlHolder(peh);
                        }
                    }
                }
                pearls.add(pd);
            }
        }
    }

    public void save() {
        File co = new File(plugin.getDataFolder(), "pearls.yml");
        if (!co.exists()) {
            try {
                co.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(co);
        for (PearlData pearlData : pearls) {
            c.set("pearls." + pearlData.getDesignation() + ".uuid", pearlData.getUuid().toString());
            c.set("pearls." + pearlData.getDesignation() + ".killer", pearlData.getKiller());
            c.set("pearls." + pearlData.getDesignation() + ".time", pearlData.getTimeKilled());
            c.set("pearls." + pearlData.getDesignation() + ".lasttime", pearlData.getLastRefuel());
            c.set("pearls." + pearlData.getDesignation() + ".fuel", pearlData.getFuel());
            if (pearlData.getPearlHolder() instanceof PearlEntityHolder) {
                c.set("pearls." + pearlData.getDesignation() + ".holder", ((PearlEntityHolder) pearlData.getPearlHolder()).getUuid().toString());
            } else if (pearlData.getPearlHolder() instanceof PearlBlockHolder) {
                c.set("pearls." + pearlData.getDesignation() + ".holder", ((PearlBlockHolder) pearlData.getPearlHolder()).getChest());
            } else {
                CivvieAPI.getInstance().getPlugin().getLogger().info("Failed to find holder for " + pearlData.getDesignation() + " because holder was a " + pearlData.getPearlHolder().getClass().getName());
            }
        }
        try {
            c.save(co);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PearlData createPearl(OfflinePlayer player, long time, String killer, int fuel) {
        StringBuilder codename = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            codename.append(CODENAME[ThreadLocalRandom.current().nextInt(CODENAME.length)]);
        }

        PearlData pearlData = new PearlData(player.getUniqueId(), codename.toString(), time, System.currentTimeMillis(), killer, fuel);
        pearls.add(pearlData);
        return pearlData;
    }

    public PearlData getPearlData(String designation) {
        for (PearlData pd : pearls) {
            if (pd.getDesignation().equals(designation))
                return pd;
        }
        return null;
    }

    public PearlData getPearlData(UUID uuid) {
        for (PearlData pd : pearls) {
            if (pd.getUuid().equals(uuid))
                return pd;
        }
        return null;
    }

    public void freePearl(PearlData pearlData) {
        File co = new File(plugin.getDataFolder(), "pearls.yml");
        if (!co.exists()) {
            try {
                co.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration c = YamlConfiguration.loadConfiguration(co);
        c.set("pearls." + pearlData.getDesignation(), null);
        this.pearls.remove(pearlData);
    }

    public class PearlData {
        private UUID uuid;
        private String designation;
        private long timeKilled;
        private long lastRefuel;
        private String killer;
        private double fuel;
        private PearlHolder pearlHolder;

        public PearlData(UUID uuid, String des, long timeKilled, long lastRefuel, String killer, double fuel) {
            this.uuid = uuid;
            this.designation = des;
            this.timeKilled = timeKilled;
            this.killer = killer;
            this.fuel = fuel;
            this.lastRefuel = lastRefuel;
        }

        public long getLastRefuel() {
            return lastRefuel;
        }

        public void setLastRefuel(long lastRefuel) {
            this.lastRefuel = lastRefuel;
        }

        public double getFuel() {
            return fuel;
        }

        public long getTimeKilled() {
            return timeKilled;
        }

        public String getKiller() {
            return killer;
        }

        public void setFuel(double fuel) {
            this.fuel = fuel;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getDesignation() {
            return designation;
        }

        public PearlHolder getLocation() {
            return pearlHolder;
        }

        public void setPearlHolder(PearlHolder pearlHolder) {
            this.pearlHolder = pearlHolder;
        }

        public PearlHolder getPearlHolder() {
            return pearlHolder;
        }

        /**
         *
         * @return whether the item needs to be removed.
         */
        public boolean updateFuel() {
            long wait = System.currentTimeMillis()-lastRefuel;
            double wait2 = ((double)wait)/(1000*60*60*24);

            setLastRefuel(System.currentTimeMillis());
            setFuel(Math.max(0,getFuel()-wait2));
            if(getFuel()<=0){
                CivvieAPI.getInstance().getPearlManager().freePearl(this);
                return true;
            }
            return false;
        }
    }

    public static abstract class PearlHolder {
        private PearlData pearlData;

        public PearlHolder(PearlData pearlData) {
            this.pearlData = pearlData;
        }

        public PearlData getPearlData() {
            return pearlData;
        }

        public abstract int getZ();

        public abstract int getY();

        public abstract int getX();
    }

    public static class PearlEntityHolder extends PearlHolder {

        private InventoryHolder holder;
        private UUID uuid;
        private Entity entity;

        public PearlEntityHolder(PearlData pearlData, Entity entity) {
            super(pearlData);
            if (entity instanceof InventoryHolder) {
                this.holder = (InventoryHolder) entity;
            }
            this.uuid = entity.getUniqueId();
            this.entity = entity;
        }

        public UUID getUuid() {
            return uuid;
        }

        public InventoryHolder getHolder() {
            return holder;
        }

        @Override
        public int getZ() {
            if (holder == null) {
                return entity.getLocation().getBlockZ();
            }
            Entity inventoryHolder = (Entity) holder;
            return inventoryHolder.getLocation().getBlockZ();
        }

        @Override
        public int getY() {
            if (holder == null) {
                return entity.getLocation().getBlockY();
            }
            Entity inventoryHolder = (Entity) holder;
            return inventoryHolder.getLocation().getBlockY();
        }

        @Override
        public int getX() {
            if (holder == null) {
                return entity.getLocation().getBlockX();
            }
            Entity inventoryHolder = (Entity) holder;
            return inventoryHolder.getLocation().getBlockX();
        }
    }

    public static class PearlBlockHolder extends PearlHolder {

        private Location chest;

        public PearlBlockHolder(PearlData pearlData, Location chest) {
            super(pearlData);
            this.chest = chest;
        }

        public Location getChest() {
            return chest;
        }

        @Override
        public int getZ() {
            return chest.getBlockZ();
        }

        @Override
        public int getY() {
            return chest.getBlockY();
        }

        @Override
        public int getX() {
            return chest.getBlockX();
        }
    }
}
