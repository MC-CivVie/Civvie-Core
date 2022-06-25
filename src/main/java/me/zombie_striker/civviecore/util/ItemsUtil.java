package me.zombie_striker.civviecore.util;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.managers.ItemManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public static ItemStack createItem(Material material, String name, int amount, String... lore){
        ItemStack is = new ItemStack(material,amount);
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(name));
        List<Component> loreC = new LinkedList<>();
        for(String l : lore){
            loreC.add(Component.text(l));
        }
        im.lore(loreC);
        is.setItemMeta(im);
        return is;
    }

    public static List<ItemManager.ItemStorage> stringListToItemTypeList(List<String> strings) {
        List <ItemManager.ItemStorage> result = new LinkedList<>();
        for(String s : strings){
            String[] split = s.split("\\,");
            ItemManager.ItemType it = CivCore.getInstance().getItemManager().getItemTypeByName(s);
            int amount = 1;
            if(split.length > 1){
                amount = Integer.parseInt(split[1]);
            }
            result.add(new ItemManager.ItemStorage(it,amount));
        }
        return result;
    }

    public static boolean containsItemStorage(List<ItemManager.ItemStorage> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for(int stack = 0; stack < stacks.size(); stack++){
            b[stack] = stacks.get(stack).getAmount();
        }


        for(int slot = 0 ; slot < inventory.getSize();slot++){
            ItemStack is = inventory.getItem(slot);
            if(is!=null){
                for(int i = 0; i < stacks.size(); i++){
                    ItemManager.ItemStorage ii = stacks.get(i);
                    if(b[i]>0 && ii.getItemType().isType(is)){
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

    public static void removeItemStorage(List<ItemManager.ItemStorage> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for(int stack = 0; stack < stacks.size(); stack++){
            b[stack] = stacks.get(stack).getAmount();
        }


        for(int slot = 0 ; slot < inventory.getSize();slot++){
            ItemStack is = inventory.getItem(slot);
            if(is!=null){
                for(int i = 0; i < stacks.size(); i++){
                    ItemManager.ItemStorage ii = stacks.get(i);
                    if(b[i]>0 && ii.getItemType().isType(is)){
                        if(b[i] < is.getAmount()) {
                            is.setAmount(is.getAmount()-b[i]);
                            b[i] = 0;
                            inventory.setItem(slot,is);
                        }else if (b[i] == is.getAmount()){
                            b[i]=0;
                            inventory.setItem(slot,null);
                        }else{
                            b[i]=b[i]-is.getAmount();
                            inventory.setItem(slot,null);
                        }
                    }
                }
            }
        }
    }
    public static void removeItems(List<ItemStack> stacks, Inventory inventory) {
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
                        if(b[i] < is.getAmount()) {
                            is.setAmount(is.getAmount()-b[i]);
                            b[i] = 0;
                            inventory.setItem(slot,is);
                        }else if (b[i] == is.getAmount()){
                            b[i]=0;
                            inventory.setItem(slot,null);
                        }else{
                            b[i]=b[i]-is.getAmount();
                            inventory.setItem(slot,null);
                        }
                    }
                }
            }
        }
    }
}
