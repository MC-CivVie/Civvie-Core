package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.data.CivChunk;
import me.zombie_striker.civviecore.data.CivWorld;
import me.zombie_striker.civviecore.data.FactoryBuild;
import me.zombie_striker.civviecore.data.FactoryRecipe;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FactoryManager {

    private List<FactoryType> types = new LinkedList<>();
    private List<FactoryRecipe> recipes = new LinkedList<>();

    public FactoryManager(CivvieCorePlugin core) {


        File folderRecipes = new File(core.getDataFolder(), "recipes");
        if (!folderRecipes.exists())
            folderRecipes.mkdirs();
        for (File file : folderRecipes.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                List<String> ingredients = config.getStringList("ingredients");
                List<ItemManager.ItemStorage> itemsIngreidents = ItemsUtil.stringListToItemTypeList(ingredients);
                List<String> results = config.getStringList("results");
                List<ItemStack> itemsResults = ItemsUtil.stringListToItemStackList(results);

                Material material = Material.matchMaterial(config.getString("icon.material"));
                String iconname = config.getString("icon.name");

                int ticktime = config.getInt("burnticks");

                ItemStack icon = ItemsUtil.createItem(material,iconname,1);

                FactoryRecipe fr = new FactoryRecipe(name, itemsResults, itemsIngreidents, icon,ticktime);
                recipes.add(fr);
            }
        }


        File folder = new File(core.getDataFolder(), "factories");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String factoryName = config.getString("name");
                List<String> ingredients = config.getStringList("ingredients");
                List<ItemStack> itemsIngreidents = ItemsUtil.stringListToItemStackList(ingredients);

                FactoryType ft = new FactoryType(factoryName);
                for (ItemStack ing : itemsIngreidents) {
                    ft.addIngredient(ing);
                }

                List<String> recipesS = config.getStringList("recipes");
                for(String recipe : recipesS){
                    for(FactoryRecipe fr : recipes){
                        if(fr.getName().equalsIgnoreCase(recipe)){
                            ft.addRecipe(fr);
                            break;
                        }
                    }
                }

            }
        }
    }

    public void tick(){
        for(CivWorld civWorld: CivCore.getInstance().getWorlds()){
            for(CivChunk cc : civWorld.getChunks()){
                for(FactoryBuild fb : cc.getFactories()){
                    if(fb.isRunning()){
                        fb.tick();
                    }
                }
            }
        }
    }



    public List<FactoryType> getTypes() {
        return types;
    }

    public List<FactoryRecipe> getRecipes() {
        return recipes;
    }

    public FactoryType getFactoryTypeByName(String factoryType) {
        for(FactoryType ft :types){
            if(ft.getName().equalsIgnoreCase(factoryType))
                return ft;
        }
        return null;
    }

    public FactoryRecipe getRecipeByName(String recipe) {
        for(FactoryRecipe fr : recipes){
            if(fr.getName().equalsIgnoreCase(recipe)){
                return fr;
            }
        }
        return null;
    }

    public class FactoryType {

        private List<ItemStack> ingredients = new LinkedList<>();
        private List<FactoryRecipe> recipes = new LinkedList<>();
        private String name;

        public FactoryType(String name) {
            this.name = name;
        }

        public void addIngredient(ItemStack ingredient) {
            this.ingredients.add(ingredient);
        }

        public void addRecipe(FactoryRecipe recipe) {
            this.recipes.add(recipe);
        }

        public List<FactoryRecipe> getRecipes() {
            return recipes;
        }

        public List<ItemStack> getIngredients() {
            return ingredients;
        }

        public String getName() {
            return name;
        }
    }
}
