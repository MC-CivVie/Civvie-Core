package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class GrowthManager {


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

    private List<BiomeGrowth> growthList = new LinkedList<>();

    public GrowthManager(CivvieCorePlugin core) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(new File(core.getDataFolder(), "cropstats.yml"));
        if(config.contains("default")){
            growTimeWheat = toLongTime(getString(core.getLogger(),config,"default.wheat.growtime"));
            growTimeBeetroot = toLongTime(getString(core.getLogger(),config,"default.beetroot.growtime"));
            growTimePotatoes = toLongTime(getString(core.getLogger(),config,"default.potato.growtime"));
            growTimeCarrots = toLongTime(getString(core.getLogger(),config,"default.carrot.growtime"));
            growTimeOak = toLongTime(getString(core.getLogger(),config,"default.oak.growtime"));
            growTimeSpruce = toLongTime(getString(core.getLogger(),config,"default.spruce.growtime"));
            growTimeBirch = toLongTime(getString(core.getLogger(),config,"default.birch.growtime"));
            growTimeJungle = toLongTime(getString(core.getLogger(),config,"default.jungle.growtime"));
            growTimeDarkOak = toLongTime(getString(core.getLogger(),config,"default.darkoak.growtime"));
            growTimeAcacia = toLongTime(getString(core.getLogger(),config,"default.acacia.growtime"));
            growTimeMangrove = toLongTime(getString(core.getLogger(),config,"default.mangrove.growtime"));
        }
        if (config.contains("biome")) {
            for (String key : config.getConfigurationSection("biome").getKeys(false)) {
                double wheat = getDouble(core.getLogger(),config,"biome." + key + ".wheat.growtime");
                double beetroot = getDouble(core.getLogger(),config,"biome." + key + ".beetroot.growtime");
                double potato = getDouble(core.getLogger(),config,"biome." + key + ".potato.growtime");
                double carrot = getDouble(core.getLogger(),config,"biome." + key + ".carrot.growtime");
                double oak = getDouble(core.getLogger(),config,"biome." + key + ".oak.growtime");
                double spruce = getDouble(core.getLogger(),config,"biome." + key + ".spruce.growtime");
                double birch = getDouble(core.getLogger(),config,"biome." + key + ".birch.growtime");
                double jungle = getDouble(core.getLogger(),config,"biome." + key + ".jungle.growtime");
                double darkoak = getDouble(core.getLogger(),config,"biome." + key + ".darkoak.growtime");
                double acacia = getDouble(core.getLogger(),config,"biome." + key + ".acacia.growtime");
                double mangrove = getDouble(core.getLogger(),config,"biome." + key + ".mangrove.growtime");
                BiomeGrowth bg = new BiomeGrowth(key, wheat, beetroot, potato, carrot, oak, spruce, birch, jungle, darkoak, acacia, mangrove);
                growthList.add(bg);
            }
        }
    }

    private double getDouble(Logger logger, FileConfiguration config, String path){
        if(config.contains(path)){
            return config.getDouble(path);
        }
        logger.info("Failed to find \""+path+"\"");
        return -1;
    }
    private String getString(Logger logger, FileConfiguration config, String path){
        if(config.contains(path)){
            return config.getString(path);
        }
        logger.info("Failed to find \""+path+"\"");
        return "-1h";
    }

    private long toLongTime(String string) {
        long time = 0;
        if(string==null)
            return 0;
        if(string.contains(" ")) {
            String[] split = string.split(" ");
            for (String s : split) {
                long l = Long.parseLong(s.substring(0, s.length() - 1));
                if (s.toLowerCase().endsWith("s")) {
                    time += l * 1000;
                } else if (s.toLowerCase().endsWith("m")) {
                    time += l * 1000 * 60;
                } else if (s.toLowerCase().endsWith("h")) {
                    time += l * 1000 * 60 * 60;
                } else if (s.toLowerCase().endsWith("d")) {
                    time += l * 1000 * 60 * 60 * 24;
                }
            }
        }else{
            long l = Long.parseLong(string.substring(0, string.length() - 1));
            if (string.toLowerCase().endsWith("s")) {
                    time += l * 1000;
                } else if (string.toLowerCase().endsWith("m")) {
                    time += l * 1000 * 60;
                } else if (string.toLowerCase().endsWith("h")) {
                    time += l * 1000 * 60 * 60;
                } else if (string.toLowerCase().endsWith("d")) {
                    time += l * 1000 * 60 * 60 * 24;
                }
        }
        return time;
    }

    public Biome getBiomeAt(Location location){
        if(CivvieAPI.getInstance().getDependancyManager().hasTerra()){
            return CivvieAPI.getInstance().getDependancyManager().getTerraManager().getBiomeName(location);
        }else{
            return location.getBlock().getBiome();
        }
    }

    public long getGrowthFor(Material type, Location location) {
        for (BiomeGrowth bg : growthList) {
            if (bg.getBiome().equalsIgnoreCase(getBiomeAt(location).getKey().value())) {
                switch (type) {
                    case BEETROOT:
                    case BEETROOTS:
                        return (long) (growTimeBeetroot*bg.growTimeBeetroot);
                    case WHEAT:
                    case WHEAT_SEEDS:
                        return (long) (growTimeWheat*bg.growTimeWheat);
                    case POTATO:
                    case POTATOES:
                        return (long) (growTimePotatoes*bg.growTimePotatoes);
                    case CARROT:
                    case CARROTS:
                        return (long) (growTimeCarrots*bg.growTimeCarrots);
                    case OAK_SAPLING:
                        return (long) (growTimeOak*bg.growTimeOak);
                    case DARK_OAK_SAPLING:
                        return (long) (growTimeDarkOak*bg.growTimeDarkOak);
                    case BIRCH_SAPLING:
                        return (long) (growTimeBirch*bg.growTimeBirch);
                    case SPRUCE_SAPLING:
                        return (long) (growTimeSpruce*bg.growTimeSpruce);
                    case JUNGLE_SAPLING:
                        return (long) (growTimeJungle*bg.growTimeJungle);
                    case ACACIA_SAPLING:
                        return (long) (growTimeAcacia*bg.growTimeAcacia);
                    case MANGROVE_PROPAGULE:
                        return (long) (growTimeMangrove*bg.growTimeMangrove);
                    default:
                        return -1;
                }
            }
        }
        CivvieAPI.getInstance().getPlugin().getLogger().info("Failed to find biome: "+getBiomeAt(location).getKey().value());
        switch (type) {
            case BEETROOT:
            case BEETROOTS:
                return (long) (growTimeBeetroot);
            case WHEAT:
            case WHEAT_SEEDS:
                return (long) (growTimeWheat);
            case POTATO:
            case POTATOES:
                return (long) (growTimePotatoes);
            case CARROT:
            case CARROTS:
                return (long) (growTimeCarrots);
            case OAK_SAPLING:
                return (long) (growTimeOak);
            case DARK_OAK_SAPLING:
                return (long) (growTimeDarkOak);
            case BIRCH_SAPLING:
                return (long) (growTimeBirch);
            case SPRUCE_SAPLING:
                return (long) (growTimeSpruce);
            case JUNGLE_SAPLING:
                return (long) (growTimeJungle);
            case ACACIA_SAPLING:
                return (long) (growTimeAcacia);
            case MANGROVE_PROPAGULE:
                return (long) (growTimeMangrove);
            default:
                return 1;
        }
    }

    public class BiomeGrowth {

        private double growTimeWheat;
        private double growTimeBeetroot;
        private double growTimePotatoes;
        private double growTimeCarrots;
        private double growTimeOak;
        private double growTimeSpruce;
        private double growTimeBirch;
        private double growTimeJungle;
        private double growTimeDarkOak;
        private double growTimeAcacia;
        private double growTimeMangrove;

        private String biome;

        public BiomeGrowth(String biome, double wh, double br, double potato, double carrot, double oak, double spruce, double birch, double jungle, double darkoak, double acaica, double mangrove) {
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

        public String getBiome() {
            return biome;
        }

        public double getGrowTimeAcacia() {
            return growTimeAcacia;
        }

        public double getGrowTimeBeetroot() {
            return growTimeBeetroot;
        }

        public double getGrowTimeBirch() {
            return growTimeBirch;
        }

        public double getGrowTimeCarrots() {
            return growTimeCarrots;
        }

        public double getGrowTimeDarkOak() {
            return growTimeDarkOak;
        }

        public double getGrowTimeJungle() {
            return growTimeJungle;
        }

        public double getGrowTimeMangrove() {
            return growTimeMangrove;
        }

        public double getGrowTimeOak() {
            return growTimeOak;
        }

        public double getGrowTimePotatoes() {
            return growTimePotatoes;
        }

        public double getGrowTimeSpruce() {
            return growTimeSpruce;
        }

        public double getGrowTimeWheat() {
            return growTimeWheat;
        }
    }

}
