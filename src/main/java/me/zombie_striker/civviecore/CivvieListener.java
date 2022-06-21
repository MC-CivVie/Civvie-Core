package me.zombie_striker.civviecore;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import java.io.File;

public class CivvieListener implements Listener {

    private final CivvieCorePlugin plugin;

    public CivvieListener(CivvieCorePlugin civvieCorePlugin) {
        this.plugin = civvieCorePlugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        CivWorld world = CivCore.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {
                CivBlock block = chunk.getBlockAt(event.getBlock().getLocation());
                if (block != null) {
                    if (block.getMaxReinforcement() > 0) {
                        if (block.getReinforcement() >= 0) {
                            event.setCancelled(true);
                            CivCore.getInstance().playReinforceProtection(event.getBlock().getLocation());
                            block.setReinforcement(block.getReinforcement() - 1);
                        }
                    }
                }else{
                    if(event.getBlock().getType()==Material.STONE || event.getBlock().getType()==Material.DEEPSLATE){
                        OreDiscoverUtil.populateOres(event.getBlock());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

    }

    @EventHandler
    public void onChunkGenerate(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -63; y < 256; y++) {
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType().name().contains("_ORE")) {
                            if (block.getType().name().contains("DEEPSLATE")) {
                                block.setType(Material.DEEPSLATE);
                            } else {
                                block.setType(Material.STONE);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        CivWorld world = CivCore.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {
                Material type = event.getBlockPlaced().getType();

                NameLayer nl = CivCore.getInstance().getReinforcingTo().get(event.getPlayer().getUniqueId());
                if (nl == null) {

                    if(event.getBlockPlaced().getType()==Material.STONE){
                        CivBlock cb = chunk.getBlockAt(event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(null);
                        cb.setMaxReinforcement(-1);
                        cb.setReinforcement(-1);
                    }
                    if(event.getBlockPlaced().getType()==Material.DEEPSLATE){
                        CivBlock cb = chunk.getBlockAt(event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(null);
                        cb.setMaxReinforcement(-1);
                        cb.setReinforcement(-1);
                    }


                    return;
                }
                Material reinfmat = CivCore.getInstance().getReinforceMaterial().get(event.getPlayer().getUniqueId());

                if (!event.getPlayer().getInventory().contains(reinfmat)) {
                    return;
                }


                switch (type) {
                    case BEETROOT_SEEDS:
                    case MELON_SEEDS:
                    case PUMPKIN_SEEDS:
                    case WHEAT_SEEDS:
                    case POTATOES:
                    case CARROTS:
                        CivBlock civBlock = chunk.getBlockAt(event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        if (civBlock == null) {
                            civBlock = new CivBlock(chunk, event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        }
                        CropBlock cp = new CropBlock(chunk, civBlock, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cp);
                        cp.setOwner(nl);
                        cp.setMaxReinforcement(CivCore.getInstance().getReinforcelevel().get(event.getPlayer().getUniqueId()));
                        cp.setReinforcement(cp.getMaxReinforcement());
                        CivCore.getInstance().playReinforceProtection(cp.getLocation());
                        break;
                    default:
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(nl);
                        cb.setMaxReinforcement(CivCore.getInstance().getReinforcelevel().get(event.getPlayer().getUniqueId()));
                        cb.setReinforcement(cb.getMaxReinforcement());
                        CivCore.getInstance().playReinforceProtection(cb.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB)
            event.setCancelled(true);
    }
}
