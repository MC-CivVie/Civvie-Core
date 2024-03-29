package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.managers.FactoryManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CivChunk {

    private final List<CivBlock> civBlocks = new LinkedList<>();

    private final List<FactoryBuild> factories = new LinkedList<>();

    private final List<CropBlock> cropBlocks = new LinkedList<>();

    private final List<Snitch> jukeblocks = new LinkedList<>();

    private int x;
    private int z;
    private CivWorld world;

    public CivChunk(int x, int z, CivWorld world) {
        this.x = x;
        this.z = z;
        this.world = world;
    }

    public static CivChunk load(int x, int z, CivWorld world) {
        CivChunk civchunk = world.getChunkAt(x, z);
        File config = CivvieAPI.getInstance().getPlugin().getChunkData(x, z, world.getWorld().getName());

        FileConfiguration c = YamlConfiguration.loadConfiguration(config);
        if (c.contains("blocks")) {
            for (String key : c.getConfigurationSection("blocks").getKeys(false)) {
                String[] parts = key.split("\\_");
                int xb = Integer.parseInt(parts[0]);
                int yb = Integer.parseInt(parts[1]);
                int zb = Integer.parseInt(parts[2]);

                int reinforce = c.getInt("blocks." + key + ".r");
                int maxreinforce = c.getInt("blocks." + key + ".mr");
                NameLayer layer = null;
                if (c.contains("blocks." + key + ".uuid"))
                    layer = CivvieAPI.getInstance().getNameLayer(UUID.fromString(c.getString("blocks." + key + ".uuid")));

                CivBlock block = new CivBlock(civchunk, new Location(world.getWorld(), xb, yb, zb));

                block.setOwner(layer);
                block.setMaxReinforcement(maxreinforce);
                block.setReinforcement(reinforce);


                civchunk.civBlocks.add(block);
            }
        }
        if (c.contains("factory")) {
            for (String key : c.getConfigurationSection("factory").getKeys(false)) {
                String[] split = key.split("\\,");
                Location craftingTable = civchunk.stringToLocation(split[0]);
                Location furnace = civchunk.stringToLocation(split[1]);
                Location chest = civchunk.stringToLocation(split[2]);

                String factoryType = c.getString("factory." + key + ".type");
                FactoryManager.FactoryType ft = CivvieAPI.getInstance().getFactoryManager().getFactoryTypeByName(factoryType);

                FactoryRecipe factoryRecipe = c.contains("factory." + key + ".recipe") ? CivvieAPI.getInstance().getFactoryManager().getRecipeByName(c.getString("factory." + key + ".recipe")) : null;

                boolean running = c.getBoolean("factory." + key + ".running");
                if (ft != null) {
                    FactoryBuild fb = new FactoryBuild(craftingTable, furnace, chest, ft);
                    fb.setCurrentRecipe(factoryRecipe);
                    fb.setRunning(running);
                    civchunk.factories.add(fb);
                }
            }
        }
        if (c.contains("crops")) {
            for (String key : c.getConfigurationSection("crops").getKeys(false)) {
                Location croploc = civchunk.stringToLocation(key);

                long plant = c.getLong("crops." + key + ".planted");
                long growth = c.getLong("crops." + key + ".growtime");

                CropBlock cropBlock = new CropBlock(civchunk, civchunk.getBlockAt(croploc.clone().subtract(0, 1, 0)), croploc, plant, growth);
                civchunk.addCivBlock(cropBlock);
                civchunk.cropBlocks.add(cropBlock);
            }
        }
        if (c.contains("bastion")) {
            for (String key : c.getConfigurationSection("bastion").getKeys(false)) {
                Location bastionloc = civchunk.stringToLocation(key);

                int radius = c.getInt("bastion." + key + ".radius");
                NameLayer layer = c.contains("bastion." + key + ".uuid") ?
                        CivvieAPI.getInstance().getNameLayer(UUID.fromString(c.getString("blocks." + key + ".uuid"))) : null;

                BastionField field = new BastionField(bastionloc, radius, layer);
                world.addBastion(field);
            }
        }
        if (c.contains("jukebox")) {
            for (String key : c.getConfigurationSection("jukebox").getKeys(false)) {
                Location jukebox = civchunk.stringToLocation(key);

                int radius = c.getInt("jukebox." + key + ".radius");
                Snitch.JukeType type = Snitch.JukeType.valueOf(c.getString("jukebox." + key + ".type"));
                Snitch jukeBlock = new Snitch(jukebox, radius, type);
                civchunk.addJukeBlock(jukeBlock);
            }
        }
        return civchunk;
    }

    public BlockFace[] bb = {BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH};

    public void updateCrops() {
        for (CropBlock cropBlock : new LinkedList<>(cropBlocks)) {
            Block b = cropBlock.getLocation().getBlock();
            if (b.getType() == Material.AIR) {
                removeCropBlock(cropBlock);
                removeCivBlock(cropBlock);
                continue;
            }
            if (cropBlock.getGrowTime() == 0) {
                removeCropBlock(cropBlock);
                removeCivBlock(cropBlock);
                return;
            }
            if (b.getType() == Material.MELON_STEM || b.getType() == Material.PUMPKIN_STEM) {
                double growStageTime = System.currentTimeMillis() - cropBlock.getPlantTime();
                double stage = growStageTime / cropBlock.getGrowTime();
                if (cropBlock.getLocation().getBlock().getBlockData() instanceof Ageable) {
                    Ageable age = (Ageable) cropBlock.getLocation().getBlock().getBlockData();
                    int stageAge = (int) Math.min(age.getMaximumAge(), stage * age.getMaximumAge());
                    if (stageAge != age.getAge()) {
                        age.setAge(stageAge);
                        cropBlock.getLocation().getBlock().setBlockData(age);
                        if (stage == age.getMaximumAge()) {
                            age.setAge(0);
                            for (BlockFace bbb : bb) {
                                Block c = null;
                                if ((c = b.getRelative(bbb)).getType() == Material.AIR) {
                                    if (c.getRelative(BlockFace.DOWN).getType() == Material.DIRT || c.getRelative(BlockFace.DOWN).getType() == Material.GRASS_BLOCK ||
                                            c.getRelative(BlockFace.DOWN).getType() == Material.FARMLAND || c.getRelative(BlockFace.DOWN).getType() == Material.COARSE_DIRT)
                                        if (b.getType() == Material.PUMPKIN_STEM) {
                                            c.setType(Material.PUMPKIN);
                                            break;
                                        } else {
                                            c.setType(Material.MELON);
                                            break;
                                        }
                                }
                            }
                        }
                    }
                }
            } else if (b.getType() == Material.CACTUS) {
                long growStageTime = System.currentTimeMillis() - cropBlock.getPlantTime();
                double stage = growStageTime / cropBlock.getGrowTime();
                if (stage > 1) {
                    for (int i = 0; i < stage; i++) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Block c = null;
                                if ((c = b.getRelative(BlockFace.UP)).getType() == Material.AIR) {
                                    c.setType(Material.CACTUS);
                                } else if (c.getType() == Material.CACTUS) {
                                    c = c.getRelative(BlockFace.UP);
                                    c.setType(Material.CACTUS);
                                }

                            }
                        }.runTaskLater(CivvieAPI.getInstance().getPlugin(), i * 5);
                    }
                }
            } else if (b.getType() == Material.OAK_SAPLING ||
                    b.getType() == Material.BIRCH_SAPLING ||
                    b.getType() == Material.SPRUCE_SAPLING ||
                    b.getType() == Material.JUNGLE_SAPLING ||
                    b.getType() == Material.DARK_OAK_SAPLING ||
                    b.getType() == Material.ACACIA_SAPLING ||
                    b.getType() == Material.MANGROVE_PROPAGULE
            ) {
                long growStageTime = System.currentTimeMillis() - (cropBlock.getPlantTime() + cropBlock.getGrowTime());
                if (growStageTime > 0) {
                    TreeType treeType = ItemsUtil.getTreeTypeFromSapling(b);
                    if (treeType != null) {
                        List<Location> saplings = new LinkedList<>();
                        if (treeType == TreeType.JUNGLE || treeType == TreeType.DARK_OAK || treeType == TreeType.TALL_REDWOOD) {
                            if (b.getLocation().add(0, 0, 1).getBlock().getType() == b.getType())
                                b.getLocation().add(0, 0, 1).getBlock().setType(Material.AIR);
                            if (b.getLocation().add(1, 0, 0).getBlock().getType() == b.getType())
                                b.getLocation().add(1, 0, 0).getBlock().setType(Material.AIR);
                            if (b.getLocation().add(1, 0, 1).getBlock().getType() == b.getType())
                                b.getLocation().add(1, 0, 1).getBlock().setType(Material.AIR);
                        }
                        b.getLocation().getBlock().setType(Material.AIR);
                        b.getWorld().generateTree(b.getLocation(), new Random(), treeType);
                        removeCropBlock(cropBlock);
                        removeCivBlock(cropBlock);
                    }
                }
            } else {
                double growStageTime = System.currentTimeMillis() - cropBlock.getPlantTime();
                if (cropBlock.getGrowTime() <= 0) {
                    removeCropBlock(cropBlock);
                    removeCivBlock(cropBlock);
                    continue;
                }
                double stage = growStageTime / (cropBlock.getGrowTime());
                if (cropBlock.getLocation().getBlock().getBlockData() instanceof Ageable) {
                    Ageable age = (Ageable) cropBlock.getLocation().getBlock().getBlockData();
                    int stageAge = (int) Math.min(age.getMaximumAge(), stage * age.getMaximumAge());
                    if (stageAge != age.getAge()) {

                        Location test = cropBlock.getLocation().clone();
                        boolean isTopBlock = true;
                        do {
                            test = test.add(0, 1, 0);
                            if (test.getBlock().getType().isOccluding()) {
                                isTopBlock = false;
                            }
                        } while (test.getY() - cropBlock.getLocation().getY() < 100 && test.getY() < test.getWorld().getMaxHeight());
                        if (isTopBlock) {
                            age.setAge(stageAge);
                            cropBlock.getLocation().getBlock().setBlockData(age);
                        }
                    }
                }
            }
        }

    }

    public List<CropBlock> getCropBlocks() {
        return cropBlocks;
    }

    public void unload() {
        File config = CivvieAPI.getInstance().getPlugin().getChunkData(x, z, world.getWorld().getName());
        FileConfiguration c = YamlConfiguration.loadConfiguration(config);

        for (CropBlock cb : cropBlocks) {
            c.set("crops." + cb.getLocation().getBlockX() + "_" + cb.getLocation().getBlockY() + "_" + cb.getLocation().getBlockZ() + ".planted", cb.getPlantTime());
            c.set("crops." + cb.getLocation().getBlockX() + "_" + cb.getLocation().getBlockY() + "_" + cb.getLocation().getBlockZ() + ".growtime", cb.getGrowTime());
        }


        for (CivBlock cb : civBlocks) {
            c.set("blocks." + cb.getLocation().getBlockX() + "_" + cb.getLocation().getBlockY() + "_" + cb.getLocation().getBlockZ() + ".r", cb.getReinforcement());
            c.set("blocks." + cb.getLocation().getBlockX() + "_" + cb.getLocation().getBlockY() + "_" + cb.getLocation().getBlockZ() + ".mr", cb.getMaxReinforcement());
            if (cb.getOwner() != null && cb.getOwner().getNlUUID() != null)
                c.set("blocks." + cb.getLocation().getBlockX() + "_" + cb.getLocation().getBlockY() + "_" + cb.getLocation().getBlockZ() + ".uuid", cb.getOwner().getNlUUID().toString());
        }
        for (FactoryBuild fb : factories) {
            StringBuilder sb = new StringBuilder();
            sb.append(fb.getCraftingTable().getBlockX());
            sb.append("_");
            sb.append(fb.getCraftingTable().getBlockY());
            sb.append("_");
            sb.append(fb.getCraftingTable().getBlockZ());
            sb.append(",");
            sb.append(fb.getFurnace().getBlockX());
            sb.append("_");
            sb.append(fb.getFurnace().getBlockY());
            sb.append("_");
            sb.append(fb.getFurnace().getBlockZ());
            sb.append(",");
            sb.append(fb.getChest().getBlockX());
            sb.append("_");
            sb.append(fb.getChest().getBlockY());
            sb.append("_");
            sb.append(fb.getChest().getBlockZ());
            c.set("factory." + sb.toString() + ".type", fb.getType().getName());
            if (fb.getCurrentRecipe() != null)
                c.set("factory." + sb.toString() + ".recipe", fb.getCurrentRecipe().getName());
            c.set("factory." + sb.toString() + ".running", fb.isRunning());
        }
        try {
            c.save(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CivBlock getBlockAt(Location location) {
        for (CivBlock cb : civBlocks) {
            if (cb != null)
                if (cb.getLocation().equals(location))
                    return cb;
        }
        return null;
    }

    public List<CivBlock> getCivBlocks() {
        return civBlocks;
    }

    public void addCivBlock(CivBlock civBlock) {
        this.civBlocks.add(civBlock);
    }

    public void removeCivBlock(CivBlock civBlock) {
        this.civBlocks.remove(civBlock);
    }

    public CivWorld getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public List<FactoryBuild> getFactories() {
        return factories;
    }

    public Location stringToLocation(String location) {
        String[] split = location.split("\\_");

        int x = Integer.parseInt(split[0]);
        int y = Integer.parseInt(split[1]);
        int z = Integer.parseInt(split[2]);
        return new Location(world.getWorld(), x, y, z);
    }

    public void addFactory(FactoryBuild fb) {
        factories.add(fb);
    }

    public void removeFactory(FactoryBuild fb) {
        factories.remove(fb);
    }

    public CropBlock getCropAt(Location location) {
        for (CropBlock cb : cropBlocks) {
            if (cb.getLocation().equals(location))
                return cb;
        }
        return null;
    }

    public void addCrop(CropBlock cp) {
        this.cropBlocks.add(cp);
    }

    public void removeCropBlock(CropBlock cblock) {
        this.cropBlocks.remove(cblock);
    }

    public List<Snitch> getJukeblocks() {
        return jukeblocks;
    }

    public Snitch getJukeBlockAt(Snitch location) {
        for (Snitch j : jukeblocks) {
            if (j.getLocation().equals(location))
                return j;
        }
        return null;
    }

    public void addJukeBlock(Snitch jukeBlock) {
        this.jukeblocks.add(jukeBlock);
    }

    public void removeJukeBlock(Snitch jukeBlock) {
        this.jukeblocks.remove(jukeBlock);
    }
}
