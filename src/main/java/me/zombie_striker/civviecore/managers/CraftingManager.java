package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CraftingManager {


    private List<RecipeRestore> restoredRecipes = new LinkedList<>();


    public CraftingManager() {

    }

    public void init(CivvieCorePlugin plugin) {

        File blacklist = new File(plugin.getDataFolder(), "blacklist");
        FileConfiguration c = YamlConfiguration.loadConfiguration(blacklist);
        List<String> blacklistedMaterials = c.getStringList("blacklist");
        for (@NotNull Iterator<Recipe> iter = Bukkit.recipeIterator(); iter.hasNext(); ) {
            Recipe rec = iter.next();
            if (blacklistedMaterials.contains(rec.getResult().getType().name())) {
                iter.remove();
            }
        }


        File folder = new File(plugin.getDataFolder(), "crafting");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                ItemManager.ItemType type = CivvieAPI.getInstance().getItemManager().getItemTypeByName(config.getString("result"));
                if (type == null) {
                    System.out.println(config.getString("result") + " is not a valid item type.");
                    continue;
                }
                boolean shapeless = config.getBoolean("shapeless");
                ItemManager.ItemType[] types = new ItemManager.ItemType[config.getConfigurationSection("ingredients").getKeys(false).size()];
                for (String s : config.getConfigurationSection("ingredients").getKeys(false)) {
                    Integer i = Integer.parseInt(s);
                    types[i - 1] = CivvieAPI.getInstance().getItemManager().getItemTypeByName(config.getString("ingredients."+s));

                }
                RecipeRestore recipeRestore;
                if (shapeless) {
                    recipeRestore = new ShapelessRecipeRestore(type, types);
                } else {
                    recipeRestore = new RecipeRestore(type, types);
                }
                if (config.contains("amount"))
                    recipeRestore.setAmount(config.getInt("amount"));
                restoredRecipes.add(recipeRestore);
                recipeRestore.register(plugin, name);

            }
        }

    }

    public List<RecipeRestore> getRestoredRecipes() {
        return restoredRecipes;
    }

    public class RecipeRestore {

        private ItemManager.ItemType itemType;
        private ItemManager.ItemType[] crafting;
        private int amount = 1;

        public RecipeRestore(ItemManager.ItemType result, ItemManager.ItemType[] crafting) {
            this.itemType = result;
            this.crafting = crafting;
        }

        public ItemManager.ItemType getItemType() {
            return itemType;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }

        public ItemManager.ItemType[] getCrafting() {
            return crafting;
        }

        public void register(CivvieCorePlugin plugin, String name) {
            ShapedRecipe shapedRecipe = new ShapedRecipe(new NamespacedKey(plugin, "civvie." + name), ItemsUtil.createItem(itemType, amount));
            if (crafting.length == 9) {
                shapedRecipe.shape("123", "456", "789");
                if (crafting[0] != null) {
                    shapedRecipe.setIngredient('1', ItemsUtil.createItem(crafting[0]));
                }
                if (crafting[1] != null) {
                    shapedRecipe.setIngredient('2', ItemsUtil.createItem(crafting[1]));
                }
                if (crafting[2] != null) {
                    shapedRecipe.setIngredient('3', ItemsUtil.createItem(crafting[2]));
                }
                if (crafting[3] != null) {
                    shapedRecipe.setIngredient('4', ItemsUtil.createItem(crafting[3]));
                }
                if (crafting[4] != null) {
                    shapedRecipe.setIngredient('5', ItemsUtil.createItem(crafting[4]));
                }
                if (crafting[5] != null) {
                    shapedRecipe.setIngredient('6', ItemsUtil.createItem(crafting[5]));
                }
                if (crafting[6] != null) {
                    shapedRecipe.setIngredient('7', ItemsUtil.createItem(crafting[6]));
                }
                if (crafting[7] != null) {
                    shapedRecipe.setIngredient('8', ItemsUtil.createItem(crafting[7]));
                }
                if (crafting[8] != null) {
                    shapedRecipe.setIngredient('9', ItemsUtil.createItem(crafting[8]));
                }
            } else {

                shapedRecipe.shape("12", "34");
                if (crafting.length >= 1 && crafting[0] != null) {
                    shapedRecipe.setIngredient('1', ItemsUtil.createItem(crafting[0]));
                }
                if (crafting.length >= 2 && crafting[1] != null) {
                    shapedRecipe.setIngredient('2', ItemsUtil.createItem(crafting[1]));
                }
                if (crafting.length >= 3 && crafting[2] != null) {
                    shapedRecipe.setIngredient('3', ItemsUtil.createItem(crafting[2]));
                }
                if (crafting.length >= 4 && crafting[3] != null) {
                    shapedRecipe.setIngredient('4', ItemsUtil.createItem(crafting[3]));
                }
            }
            Bukkit.addRecipe(shapedRecipe);
        }

        public boolean isRecipe(ItemStack[] matrix) {
            if (crafting.length == 9) {
                for (int i = 0; i < crafting.length; i++)
                    if ((getCrafting()[i] == null && matrix[i] != null) || !getCrafting()[i].isType(matrix[i]))
                        return false;
                return true;
            } else if (crafting.length == 4) {
                int firstused = 0;
                while (firstused < matrix.length-2) {
                    if (matrix[firstused] != null)
                        break;
                    firstused++;
                }
                if ((getCrafting()[firstused] == null && matrix[firstused] != null) || !getCrafting()[firstused].isType(matrix[firstused])){
                    return false;
                }
                if ((getCrafting()[firstused + 1] == null && matrix[firstused + 1] != null) || !getCrafting()[firstused + 1].isType(matrix[firstused + 1])){
                    return false;
                }
                if (matrix[firstused + 2] != null || matrix.length <= firstused+4) {

                    if ((getCrafting()[firstused + 2] == null && matrix[firstused + 2] != null) || !getCrafting()[firstused + 2].isType(matrix[firstused + 2])){{
                            return false;
                        }
                    }
                    if ((getCrafting()[firstused + 3] == null && matrix[firstused + 3] != null) || !getCrafting()[firstused + 3].isType(matrix[firstused + 3])){
                        return false;
                    }
                } else {
                    if ((getCrafting()[firstused + 2] == null && matrix[firstused + 3] != null) || !getCrafting()[firstused + 2].isType(matrix[firstused + 3]))
                        return false;
                    if ((getCrafting()[firstused + 3] == null && matrix[firstused + 4] != null) || !getCrafting()[firstused + 3].isType(matrix[firstused + 4]))
                        return false;

                }
                return true;
            }
            return false;
        }
    }

    public class ShapelessRecipeRestore extends RecipeRestore {

        public ShapelessRecipeRestore(ItemManager.ItemType result, ItemManager.ItemType[] crafting) {
            super(result, crafting);
        }

        @Override
        public void register(CivvieCorePlugin plugin, String name) {
            try {
                ShapelessRecipe shapedRecipe = new ShapelessRecipe(new NamespacedKey(plugin, "civvie." + name), ItemsUtil.createItem(getItemType()));
                for (int i = 0; i < getCrafting().length; i++) {
                    if (getCrafting()[i] != null)
                        shapedRecipe.addIngredient(ItemsUtil.createItem(getCrafting()[i]));

                }
            } catch (Exception e4) {
                e4.printStackTrace();
            }
        }

        public boolean isRecipe(ItemStack[] matrix) {
            boolean[] taken = new boolean[matrix.length];
            for (int i = 0; i < matrix.length; i++) {
                if (getCrafting().length >= i || getCrafting()[i] == null) {
                    taken[i] = true;
                    continue;
                }
                boolean found = false;
                for (int j = 0; j < taken.length; j++) {
                    if (!taken[i]) {
                        if (getCrafting()[j].isType(matrix[i])) {
                            found = true;
                            break;
                        }
                    }
                }
                taken[i] = found;
            }
            for (int b = 0; b < taken.length; b++) {
                if (!taken[b])
                    return false;
            }
            return true;
        }
    }
}
