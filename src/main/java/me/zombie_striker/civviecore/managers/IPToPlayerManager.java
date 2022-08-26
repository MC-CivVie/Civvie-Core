package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class IPToPlayerManager {

    private List<IPHolder> ipHolders = new LinkedList<>();

    public IPToPlayerManager(CivvieCorePlugin plugin) {
        File file = new File(plugin.getDataFolder(), "ips.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (config.contains("ips")) {
            for (String key : config.getConfigurationSection("ips").getKeys(false)) {
                String ip = config.getString("ips." + key + ".ip");
                long ll = config.getLong("ips." + key + ".ll");
                List<String> players = config.getStringList("ips." + key + ".uuid");
                List<UUID> uuids = new LinkedList<>();
                UUID keyuuid = UUID.fromString(key);

                for (String player : players) {
                    uuids.add(UUID.fromString(player));
                }
                IPHolder ipHolder = new IPHolder(keyuuid, uuids, ip,ll);
                ipHolders.add(ipHolder);
            }
        }
    }

    public void save() {
        File file = new File(CivvieAPI.getInstance().getPlugin().getDataFolder(), "ips.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (IPHolder ipHolder : ipHolders) {
            config.set("ips." + ipHolder.getKey().toString() + ".ip", ipHolder.getIp());
            List<String> uuids = new LinkedList<>();
            for (UUID uuid : ipHolder.getUuids()) {
                uuids.add(uuid.toString());
            }
            config.set("ips." + ipHolder.getKey().toString() + ".uuid", uuids);
            config.set("ips." + ipHolder.getKey().toString() + ".ll", ipHolder.getLastLogin());
        }
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<IPHolder> getIpHolders() {
        return ipHolders;
    }

    public void addIPHolder(IPHolder ipHolder) {
        this.ipHolders.add(ipHolder);
    }

    public List<UUID> getUUIDsFor(String hostAddress) {
        for (IPHolder ipHolder : getIpHolders()) {
            if (ipHolder.getIp().equals(hostAddress))
                return ipHolder.getUuids();
        }
        return null;
    }

    public static class IPHolder {
        private String ip;
        private List<UUID> uuids;
        private UUID key;

        private long lastLogin;

        public IPHolder(UUID key, List<UUID> uuids, String ip, long lastLogin) {
            this.ip = ip;
            this.uuids = uuids;
            this.key = key;
            this.lastLogin = lastLogin;
        }

        public UUID getKey() {
            return key;
        }

        public List<UUID> getUuids() {
            return uuids;
        }

        public String getIp() {
            return ip;
        }

        public long getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(long lastLogin) {
            this.lastLogin = lastLogin;
        }
    }

}
