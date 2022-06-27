package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
            growTimeWheat = toLongTime(config.getString("default.wheat.growtime"));
            growTimeBeetroot = toLongTime(config.getString("default.beetroot.growtime"));
            growTimePotatoes = toLongTime(config.getString("default.potato.growtime"));
            growTimeCarrots = toLongTime(config.getString("default.carrot.growtime"));
            growTimeOak = toLongTime(config.getString("default.oak.growtime"));
            growTimeSpruce = toLongTime(config.getString("default.spruce.growtime"));
            growTimeBirch = toLongTime(config.getString("default.birch.growtime"));
            growTimeJungle = toLongTime(config.getString("default.jungle.growtime"));
            growTimeDarkOak = toLongTime(config.getString("default.darkoak.growtime"));
            growTimeAcacia = toLongTime(config.getString("default.acacia.growtime"));
            growTimeMangrove = toLongTime(config.getString("default.mangrove.growtime"));
        }
        if (config.contains("biome")) {
            for (String key : config.getConfigurationSection("biome").getKeys(false)) {
                double wheat = config.getDouble("biome." + key + ".wheat.growtime");
                double beetroot = config.getDouble("biome." + key + ".beetroot.growtime");
                double potato = config.getDouble("biome." + key + ".potato.growtime");
                double carrot = config.getDouble("biome." + key + ".carrot.growtime");
                double oak = config.getDouble("biome." + key + ".oak.growtime");
                double spruce = config.getDouble("biome." + key + ".spruce.growtime");
                double birch = config.getDouble("biome." + key + ".birch.growtime");
                double jungle = config.getDouble("biome." + key + ".jungle.growtime");
                double darkoak = config.getDouble("biome." + key + ".darkoak.growtime");
                double acacia = config.getDouble("biome." + key + ".acacia.growtime");
                double mangrove = config.getDouble("biome." + key + ".mangrove.growtime");
                BiomeGrowth bg = new BiomeGrowth(key, wheat, beetroot, potato, carrot, oak, spruce, birch, jungle, darkoak, acacia, mangrove);
                growthList.add(bg);
            }
        }
    }

    private long toLongTime(String string) {
        long time = 0;
        if(string==null)
            return 0;
        if(string.contains(" ")) {
            String[] split = string.split(" ");
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
        }else{
                if (string.toLowerCase().endsWith("s")) {
                    time += Long.parseLong(string.substring(0, string.length() - 1)) * 1000;
                } else if (string.toLowerCase().endsWith("m")) {
                    time += Long.parseLong(string.substring(0, string.length() - 1)) * 1000 * 60;
                } else if (string.toLowerCase().endsWith("h")) {
                    time += Long.parseLong(string.substring(0, string.length() - 1)) * 1000 * 60 * 60;
                } else if (string.toLowerCase().endsWith("d")) {
                    time += Long.parseLong(string.substring(0, string.length() - 1)) * 1000 * 60 * 60 * 24;
                }
        }
        return time;
    }

    public Biome getBiomeAt(Location location){
        if(CivCore.getInstance().getDependancyManager().hasTerra()){
            return CivCore.getInstance().getDependancyManager().getTerraManager().getBiomeName(location);
        }else{
            return location.getBlock().getBiome();
        }
    }

    public long getGrowthFor(Material type, Location location) {
        for (BiomeGrowth bg : growthList) {


            if (bg.getBiome().equalsIgnoreCase(getBiomeAt(location).getKey().getNamespace())) {
                switch (type) {
                    case BEETROOT:
                        return (long) (growTimeBeetroot*bg.growTimeBeetroot);
                    case WHEAT:
                        return (long) (growTimeWheat*bg.growTimeWheat);
                    case POTATO:
                        return (long) (growTimePotatoes*bg.growTimePotatoes);
                    case CARROT:
                        return (long) (growTimeCarrots*bg.growTimeCarrots);
                    case BEETROOTS:
                        return (long) (growTimeBeetroot*bg.growTimeBeetroot);
                    case WHEAT_SEEDS:
                        return (long) (growTimeWheat*bg.growTimeWheat);
                    case POTATOES:
                        return (long) (growTimePotatoes*bg.growTimePotatoes);
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
        CivCore.getInstance().getPlugin().getLogger().info("Failed to find biome: "+getBiomeAt(location).getKey().value());
        switch (type) {
            case BEETROOT:
                return (long) (growTimeBeetroot);
            case WHEAT:
                return (long) (growTimeWheat);
            case POTATO:
                return (long) (growTimePotatoes);
            case CARROT:
                return (long) (growTimeCarrots);
            case BEETROOTS:
                return (long) (growTimeBeetroot);
            case WHEAT_SEEDS:
                return (long) (growTimeWheat);
            case POTATOES:
                return (long) (growTimePotatoes);
            case CARROTS:
                return (long) (growTimeCarrots);
            case OAK_SAPLING:
                return (long) (growTimeDarkOak);
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
                return -1;
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
