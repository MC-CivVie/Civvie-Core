package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ItemManager {

    private List<ItemType> itemTypes = new LinkedList<>();

    public ItemManager(CivvieCorePlugin plugin){
        for(Material material : Material.values()){
            ItemType type = new ItemType(material,material.name());
            itemTypes.add(type);
        }
        File folder = new File(plugin.getDataFolder(),"materials");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                List<Material> material = new LinkedList<>();
                for(String s : config.getStringList("types")){
                    material.add(Material.matchMaterial(s));
                }
                ItemSubType subtype = new ItemSubType(material, name);
                itemTypes.add(subtype);
            }
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
