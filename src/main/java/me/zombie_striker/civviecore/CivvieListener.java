package me.zombie_striker.civviecore;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import me.zombie_striker.civviecore.data.CivBlock;
import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.io.File;

public class CivvieListener implements Listener {

    private final CivvieCorePlugin plugin;
    public CivvieListener(CivvieCorePlugin civvieCorePlugin) {
        this.plugin = civvieCorePlugin;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event){
        CivWorld world = CivCore.getInstance().getWorld(event.getBlock().getWorld().getName());
        if(world!=null){
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(),event.getBlock().getChunk().getZ());
            if(chunk!=null){
                CivBlock block = chunk.getBlockAt(event.getBlock().getLocation());
                if(block != null){
                    if(block.getMaxReinforcement() > 0){
                        if(block.getReinforcement() > 0){
                            event.setCancelled(true);
                            CivCore.getInstance().playReinforceProtection(event.getBlock().getLocation());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event){
        if(event.getEntityType()== EntityType.EXPERIENCE_ORB)
            event.setCancelled(true);
    }
}
