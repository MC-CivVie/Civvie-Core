package me.zombie_striker.civviecore.dependancies;

import com.dfsek.terra.api.world.ServerWorld;
import com.dfsek.terra.bukkit.world.BukkitAdapter;
import com.dfsek.terra.bukkit.world.BukkitPlatformBiome;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Location;
import org.bukkit.block.Biome;

public class TerraManager {

    public CivvieCorePlugin core;

    public TerraManager(CivvieCorePlugin core){
        this.core = core;
    }

    public Biome getBiomeName(Location location){
        ServerWorld serverWorld = BukkitAdapter.adapt(location.getWorld());
        BukkitPlatformBiome platformBiome = (BukkitPlatformBiome) serverWorld.getBiomeProvider().getBiome(location.getBlockX(),location.getBlockY(),location.getBlockZ(),location.getWorld().getSeed()).getPlatformBiome();
        return platformBiome.getHandle();
    }
}
