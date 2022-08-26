package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class OreDiscoverManager {

    private static final BlockFace[] faces = new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};


    private List<OreChanceData> oredata = new LinkedList<>();

    public void init(CivvieCorePlugin plugin) {
        File folder = new File(plugin.getDataFolder(), "ores");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                int chance = config.getInt("chance");
                Material material = Material.matchMaterial(config.getString("material"));
                Material stone = Material.matchMaterial(config.getString("stone"));
                int minY = config.getInt("miny");
                int maxY = config.getInt("maxy");
                OreChanceData data = new OreChanceData(material, chance, 0, 0, stone,minY,maxY);
                oredata.add(data);
            }
        }
    }


    public void populateOres(Block center) {
        for (BlockFace bf : faces) {
            Block rel = center.getRelative(bf);
            for (OreChanceData e : oredata) {
                if (e.getStoneType() == rel.getType()) {
                    if(rel.getLocation().getY()<=e.getMaxY()&&rel.getLocation().getY()>=e.getMinY()) {
                        int tlc = new Random().nextInt(e.getChance());
                        if (tlc == 0) {
                            rel.setType(e.getMaterial());
                            break;
                        }
                    }
                }
            }
        }
    }

    public class OreChanceData {

        private int chance;
        private int closebyChance;
        private int distanceClosebyChance;
        private int minY;
        private int maxY;
        private Material deepslate;
        private Material material;

        public OreChanceData(Material material, int chance, int closebyChance, int distanceClosebyChance, Material foundIn, int minY, int maxY) {
            this.chance = chance;
            this.closebyChance = closebyChance;
            this.distanceClosebyChance = distanceClosebyChance;
            this.deepslate = foundIn;
            this.material = material;
            this.minY = minY;
            this.maxY = maxY;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMinY() {
            return minY;
        }

        public Material getMaterial() {
            return material;
        }

        public Material getStoneType() {
            return deepslate;
        }

        public int getChance() {
            return chance;
        }

        public int getClosebyChance() {
            return closebyChance;
        }

        public int getDistanceClosebyChance() {
            return distanceClosebyChance;
        }
    }
}
