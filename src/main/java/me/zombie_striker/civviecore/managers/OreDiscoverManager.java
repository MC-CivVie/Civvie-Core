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
                OreChanceData data = new OreChanceData(material, chance, 0, 0, stone);
                oredata.add(data);
            }
        }
    }


    public void populateOres(Block center) {
        for (BlockFace bf : faces) {
            Block rel = center.getRelative(bf);
            for (OreChanceData e : oredata) {
                if (e.getStoneType() == rel.getType()) {
                    int tlc = new Random().nextInt(e.getChance());
                    if (tlc == 0) {
                        rel.setType(e.getMaterial());
                        break;
                    }
                }
            }
        }
    }

    public class OreChanceData {

        private int chance;
        private int closebyChance;
        private int distanceClosebyChance;
        private Material deepslate;
        private Material material;

        public OreChanceData(Material material, int chance, int closebyChance, int distanceClosebyChance, Material deepslate) {
            this.chance = chance;
            this.closebyChance = closebyChance;
            this.distanceClosebyChance = distanceClosebyChance;
            this.deepslate = deepslate;
            this.material = material;
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
