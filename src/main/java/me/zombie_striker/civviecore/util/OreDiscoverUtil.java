package me.zombie_striker.civviecore.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashMap;

public class OreDiscoverUtil {

    private static final BlockFace[] faces = new BlockFace[]{BlockFace.UP,BlockFace.DOWN,BlockFace.EAST,BlockFace.WEST,BlockFace.NORTH,BlockFace.SOUTH};


    private static final HashMap<Material,Integer> chanceOfAppearingStone = new HashMap<>();
    private static final HashMap<Material,Integer> chanceOfAppearingDeepslate = new HashMap<>();

    public static void init(){
        chanceOfAppearingStone.put(Material.IRON_ORE,50);
        chanceOfAppearingStone.put(Material.GOLD_ORE,200);
        chanceOfAppearingStone.put(Material.LAPIS_ORE,400);
        chanceOfAppearingStone.put(Material.DIAMOND_ORE,1000);
        chanceOfAppearingStone.put(Material.COAL_ORE,50);

        chanceOfAppearingDeepslate.put(Material.DEEPSLATE_IRON_ORE,50);
        chanceOfAppearingDeepslate.put(Material.DEEPSLATE_GOLD_ORE,200);
        chanceOfAppearingDeepslate.put(Material.DEEPSLATE_LAPIS_ORE,400);
        chanceOfAppearingDeepslate.put(Material.DEEPSLATE_DIAMOND_ORE,1000);
        chanceOfAppearingDeepslate.put(Material.DEEPSLATE_COAL_ORE,50);
    }


    public static void populateOres(Block center){
        for(BlockFace bf : faces){
            Block rel = center.getRelative(bf);
            if(rel.getType()== Material.STONE){

            }else if (rel.getType()==Material.DEEPSLATE){

            }
        }
    }
}
