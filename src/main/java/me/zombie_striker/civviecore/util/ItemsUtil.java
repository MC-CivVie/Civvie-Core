package me.zombie_striker.civviecore.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class ItemsUtil {

    public static List<ItemStack> stringListToItemStackList(List<String> strings){
        List <ItemStack> result = new LinkedList<>();
        for(String s : strings){
            String[] split = s.split("\\,");
            Material material = Material.matchMaterial(split[0]);
            int amount = 1;
            if(split.length > 1){
                amount = Integer.parseInt(split[1]);
            }
            ItemStack is = new ItemStack(material,amount);
            result.add(is);
        }
        return result;
    }
}
