package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.data.FactoryRecipe;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BreweryManager {

    private List<Brew> brews = new LinkedList<>();

    private HashMap<Location, List<ItemManager.ItemType>> worts = new HashMap<>();

    public BreweryManager(){

    }

    public void init(CivvieCorePlugin plugin){
        File folderRecipes = new File(plugin.getDataFolder(), "brews");
        if (!folderRecipes.exists())
            folderRecipes.mkdirs();
        for (File file : folderRecipes.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name;
                if(config.contains("name")) {
                    name =config.getString("name");
                }else{
                    name = file.getName().substring(0,file.getName().length()-4);
                }
                String displayname = config.getString("displayname");
                List<String> ingredients = config.getStringList("ingredients");
                List<ItemManager.ItemStorage> itemsIngreidents = ItemsUtil.stringListToItemTypeList(ingredients);
            }
        }
    }

    public HashMap<Location, List<ItemManager.ItemType>> getWorts() {
        return worts;
    }

    public List<Brew> getBrews() {
        return brews;
    }

    public class Brew{
        private String name;
        private String displayName;
        private List<ItemManager.ItemStorage> ingredients = new LinkedList<>();
        private Color color;

        public Brew(String name, String displayName, Color color){
            this.name = name;
            this.displayName = displayName;
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public List<ItemManager.ItemStorage> getIngredients() {
            return ingredients;
        }
        public void addIngredient(ItemManager.ItemStorage storage){
            this.ingredients.add(storage);
        }
    }
}
