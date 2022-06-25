package me.zombie_striker.civviecore.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
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

    public static boolean containsItems(List<ItemStack> stacks, Inventory inventory){
        int[] b = new int[stacks.size()];

        for(int stack = 0; stack < stacks.size(); stack++){
            b[stack] = stacks.get(stack).getAmount();
        }


        for(int slot = 0 ; slot < inventory.getSize();slot++){
            ItemStack is = inventory.getItem(slot);
            if(is!=null){
                for(int i = 0; i < stacks.size(); i++){
                    ItemStack ii = stacks.get(i);
                    if(b[i]>0 && ii.getType()==is.getType()){
                        if(b[i] <= is.getAmount()) {
                            b[i] = 0;
                        }else{
                            b[i]=b[i]-is.getAmount();
                        }
                    }
                }
            }
        }

        for(int i : b){
            if(i > 0)
                return false;
        }
        return true;
    }
}
