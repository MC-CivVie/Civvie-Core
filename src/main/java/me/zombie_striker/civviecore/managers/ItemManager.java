package me.zombie_striker.civviecore.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ItemManager {

    private List<ItemType> itemTypes = new LinkedList<>();

    public ItemManager(){
        for(Material material : Material.values()){
            ItemType type = new ItemType(material,material.name());
            itemTypes.add(type);
        }
    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public ItemType getItemTypeByName(String name){
        for(ItemType it: itemTypes){
            if(it.getName().equalsIgnoreCase(name))
                return it;
        }
        return null;
    }

    public class ItemType{
        private Material baseMaterial;
        private String name;

        public ItemType(Material baseMaterial, String name){
            this.baseMaterial = baseMaterial;
            this.name = name;
        }

        public Material getBaseMaterial() {
            return baseMaterial;
        }

        public String getName() {
            return name;
        }

        public boolean isType(ItemStack is) {
            return baseMaterial==is.getType();
        }
    }
    public class ItemSubType extends  ItemType{
        private List<Material> types;

        public ItemSubType(List<Material> materials, String name){
            super(Material.AIR,name);
            this.types = materials;
        }

        public List<Material> getTypes() {
            return types;
        }

        @Override
        public boolean isType(ItemStack is) {
            for(Material m : types){
                if(m==is.getType())
                    return true;
            }
            return false;
        }
    }

    public static class ItemStorage{

        private ItemType itemType;
        private int amount;

        public ItemStorage(ItemType itemType, int amount){
            this.amount = amount;
            this.itemType = itemType;
        }

        public ItemType getItemType() {
            return itemType;
        }

        public int getAmount() {
            return amount;
        }
    }

}
