package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class GrowthManager {


    private List<BiomeGrowth> growthList = new LinkedList<>();

    public GrowthManager(CivvieCorePlugin core) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(core.getDataFolder(), "cropstats.yml"));
        if (config.contains("biome")) {
            for (String key : config.getConfigurationSection("biome").getKeys(false)) {
                Biome biome = Biome.valueOf(key);
                long wheat = toLongTime(config.getString("biome." + key + ".wheat.growtime"));
                long beetroot = toLongTime(config.getString("biome." + key + ".beetroot.growtime"));
                long potato = toLongTime(config.getString("biome." + key + ".potato.growtime"));
                long carrot = toLongTime(config.getString("biome." + key + ".carrot.growtime"));
                long oak = toLongTime(config.getString("biome." + key + ".oak.growtime"));
                long spruce = toLongTime(config.getString("biome." + key + ".spruce.growtime"));
                long birch = toLongTime(config.getString("biome." + key + ".birch.growtime"));
                long jungle = toLongTime(config.getString("biome." + key + ".jungle.growtime"));
                long darkoak = toLongTime(config.getString("biome." + key + ".darkoak.growtime"));
                long acacia = toLongTime(config.getString("biome." + key + ".acacia.growtime"));
                long mangrove = toLongTime(config.getString("biome." + key + ".mangrove.growtime"));
                BiomeGrowth bg = new BiomeGrowth(biome, wheat, beetroot, potato, carrot, oak, spruce, birch, jungle, darkoak, acacia, mangrove);
                growthList.add(bg);
            }
        }
    }

    private long toLongTime(String string) {
        String[] split = string.split(" ");
        long time = 0;
        for (String s : split) {
            if (s.toLowerCase().endsWith("s")) {
                time += Long.parseLong(s.substring(0, s.length() - 1)) * 1000;
            } else if (s.toLowerCase().endsWith("m")) {
                time += Long.parseLong(s.substring(0, s.length() - 1)) * 1000 * 60;
            } else if (s.toLowerCase().endsWith("h")) {
                time += Long.parseLong(s.substring(0, s.length() - 1)) * 1000 * 60 * 60;
            } else if (s.toLowerCase().endsWith("d")) {
                time += Long.parseLong(s.substring(0, s.length() - 1)) * 1000 * 60 * 60 * 24;
            }
        }
        return time;
    }

    public long getGrowthFor(Material type, Biome biome) {
        for (BiomeGrowth bg : growthList) {
            if (bg.getBiome() == biome) {
                switch (type) {
                    case BEETROOTS:
                        return bg.growTimeBeetroot;
                    case WHEAT_SEEDS:
                        return bg.growTimeWheat;
                    case POTATOES:
                        return bg.growTimePotatoes;
                    case CARROTS:
                        return bg.growTimeCarrots;
                    case OAK_SAPLING:
                        return bg.growTimeOak;
                    case DARK_OAK_SAPLING:
                        return bg.growTimeDarkOak;
                    case BIRCH_SAPLING:
                        return bg.growTimeBirch;
                    case SPRUCE_SAPLING:
                        return bg.growTimeSpruce;
                    case JUNGLE_SAPLING:
                        return bg.growTimeJungle;
                    case ACACIA_SAPLING:
                        return bg.growTimeAcacia;
                    case MANGROVE_PROPAGULE:
                        return bg.growTimeMangrove;
                    default:
                        return -1;
                }
            }
        }
        return -1;
    }

    public class BiomeGrowth {

        private long growTimeWheat;
        private long growTimeBeetroot;
        private long growTimePotatoes;
        private long growTimeCarrots;
        private long growTimeOak;
        private long growTimeSpruce;
        private long growTimeBirch;
        private long growTimeJungle;
        private long growTimeDarkOak;
        private long growTimeAcacia;
        private long growTimeMangrove;

        private Biome biome;

        public BiomeGrowth(Biome biome, long wh, long br, long potato, long carrot, long oak, long spruce, long birch, long jungle, long darkoak, long acaica, long mangrove) {
            this.biome = biome;
            this.growTimeWheat = wh;
            this.growTimeBeetroot = br;
            this.growTimePotatoes = potato;
            this.growTimeCarrots = carrot;
            this.growTimeOak = oak;
            this.growTimeSpruce = spruce;
            this.growTimeBirch = birch;
            this.growTimeJungle = jungle;
            this.growTimeDarkOak = darkoak;
            this.growTimeAcacia = acaica;
            this.growTimeMangrove = mangrove;
        }

        public Biome getBiome() {
            return biome;
        }

        public long getGrowTimeAcacia() {
            return growTimeAcacia;
        }

        public long getGrowTimeBeetroot() {
            return growTimeBeetroot;
        }

        public long getGrowTimeBirch() {
            return growTimeBirch;
        }

        public long getGrowTimeCarrots() {
            return growTimeCarrots;
        }

        public long getGrowTimeDarkOak() {
            return growTimeDarkOak;
        }

        public long getGrowTimeJungle() {
            return growTimeJungle;
        }

        public long getGrowTimeMangrove() {
            return growTimeMangrove;
        }

        public long getGrowTimeOak() {
            return growTimeOak;
        }

        public long getGrowTimePotatoes() {
            return growTimePotatoes;
        }

        public long getGrowTimeSpruce() {
            return growTimeSpruce;
        }

        public long getGrowTimeWheat() {
            return growTimeWheat;
        }
    }

}
