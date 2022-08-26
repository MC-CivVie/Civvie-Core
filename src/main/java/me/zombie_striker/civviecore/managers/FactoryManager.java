package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.util.ItemsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class FactoryManager {

    private List<FactoryType> types = new LinkedList<>();
    private List<FactoryRecipe> recipes = new LinkedList<>();

    public FactoryManager(CivvieCorePlugin core) {
    }

    public void tick() {
        for (CivWorld civWorld : CivvieAPI.getInstance().getWorlds()) {
            for (CivChunk cc : civWorld.getChunks()) {
                for (FactoryBuild fb : cc.getFactories()) {
                    if (fb.isRunning()) {
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
        for (FactoryType ft : types) {
            if (ft.getName().equalsIgnoreCase(factoryType))
                return ft;
        }
        return null;
    }

    public FactoryRecipe getRecipeByName(String recipe) {
        for (FactoryRecipe fr : recipes) {
            if (fr.getName().equalsIgnoreCase(recipe)) {
                return fr;
            }
        }
        return null;
    }

    public void init(CivvieCorePlugin plugin) {

        File folderRecipes = new File(plugin.getDataFolder(), "recipes");
        if (!folderRecipes.exists())
            folderRecipes.mkdirs();
        for (File file : folderRecipes.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name;
                if (config.contains("name")) {
                    name = config.getString("name");
                } else {
                    name = file.getName().substring(0, file.getName().length() - 4);
                }
                String displayname = config.getString("displayname");
                List<String> ingredients = config.getStringList("ingredients");
                List<ItemManager.ItemStorage> itemsIngreidents = ItemsUtil.stringListToItemTypeList(ingredients);
                List<String> results = config.getStringList("results");
                List<ItemStack> itemsResults = ItemsUtil.stringListToItemStackList(results);

                Material material = Material.matchMaterial(config.getString("icon.material"));

                int ticktime = config.getInt("burnticks");
                if (material != null) {
                    List<String> lore = ItemsUtil.stringifyListItemStorage(itemsIngreidents);
                    lore.add(ChatColor.WHITE + "------------");
                    for (ItemStack re : itemsResults) {
                        if (re.getItemMeta().hasCustomModelData()) {
                            lore.add("x" + re.getAmount() + " " + CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(re.getType(), re.getItemMeta().getCustomModelData()).getName());
                        } else {
                            lore.add("x" + re.getAmount() + " " + CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(re.getType()).getName());
                        }
                    }
                    ItemStack icon = ItemsUtil.createItem(material, displayname, 1, lore);

                    FactoryRecipe fr = new FactoryRecipe(name, displayname, itemsResults, itemsIngreidents, icon, ticktime);
                    recipes.add(fr);
                }
            }
        }


        File folder = new File(plugin.getDataFolder(), "factories");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String factoryName = config.getString("name");
                String factoryDisplayName = config.getString("displayname");
                List<String> ingredients = config.getStringList("ingredients");
                List<ItemManager.ItemStorage> itemsIngreidents = ItemsUtil.stringListToItemTypeList(ingredients);

                Material icon = Material.matchMaterial(config.getString("icon"));

                FactoryType ft = new FactoryType(factoryName, icon, factoryDisplayName);
                for (ItemManager.ItemStorage ing : itemsIngreidents) {
                    ft.addIngredient(ing);
                }

                List<String> recipesS = config.getStringList("recipes");
                for (String recipe : recipesS) {
                    for (FactoryRecipe fr : recipes) {
                        if (fr.getName().equalsIgnoreCase(recipe)) {
                            ft.addRecipe(fr);
                            break;
                        }
                    }
                }
                types.add(ft);
            }
        }

        CompactorFactoryType cft = new CompactorFactoryType("compactor");
        types.add(cft);
        BastionFactoryType bft = new BastionFactoryType("bastionfactory");
        types.add(bft);
    }

    public class FactoryType {

        private final List<ItemManager.ItemStorage> ingredients = new LinkedList<ItemManager.ItemStorage>();
        private final List<FactoryRecipe> recipes = new LinkedList<>();
        private final String name;
        private final Material iconMaterial;
        private String displayname;

        public FactoryType(String name, Material iconMaterial, String displayname) {
            this.iconMaterial = iconMaterial;
            this.name = name;
            this.displayname = displayname;
        }

        public String getDisplayname() {
            return displayname;
        }

        public Material getIconMaterial() {
            return iconMaterial;
        }

        public void addIngredient(ItemManager.ItemStorage ingredient) {
            this.ingredients.add(ingredient);
        }

        public void addRecipe(FactoryRecipe recipe) {
            this.recipes.add(recipe);
        }

        public List<FactoryRecipe> getRecipes() {
            return recipes;
        }

        public List<ItemManager.ItemStorage> getIngredients() {
            return ingredients;
        }

        public String getName() {
            return name;
        }
    }

    public class CompactorFactoryType extends FactoryType {
        public CompactorFactoryType(String name) {
            super(name, Material.TRAPPED_CHEST, "Compactor");
            addIngredient(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.TRAPPED_CHEST), 16));
            addRecipe(new CompactorRecipe("compact", "Compact Items", ItemsUtil.createItem(Material.CHEST, "Compact Items", 1), 10));
            addRecipe(new DecompactorRecipe("decompact", "Decompact Items", ItemsUtil.createItem(Material.CHEST, "Decompact Items", 1), 10));
        }
    }

    public class BastionFactoryType extends FactoryType {
        public BastionFactoryType(String name) {
            super(name, Material.ENDER_CHEST, "Bastion Factory");
            addIngredient(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.OBSIDIAN), 64));
            addIngredient(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.DIAMOND), 16));
            addIngredient(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.LAPIS_BLOCK), 32));
            addIngredient(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.IRON_INGOT), 64));


            addRecipe(new CityBastionRecipe("citybastion", "Make City Bastion", Arrays.asList(ItemsUtil.createItemLoreComponent(Material.ENDER_CHEST, "City Bastion", 1, ItemsUtil.getCityLore())),
                    Arrays.asList(
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.OBSIDIAN), 64),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.DIAMOND), 16),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.LAPIS_BLOCK), 32),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.IRON_INGOT), 64)
                    )
                    , ItemsUtil.createItemLoreComponent(Material.ENDER_CHEST, "Make City Bastion", 1, ItemsUtil.getCityLore()), 10));
            addRecipe(new VaultBastionRecipe("vaultbastion", "Make Vault Bastion", Arrays.asList(ItemsUtil.createItemLoreComponent(Material.ENDER_CHEST, "Vault Bastion", 16, ItemsUtil.getVaultLore())),
                    Arrays.asList(
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.OBSIDIAN), 64),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.DIAMOND), 8),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.LAPIS_BLOCK), 16),
                            new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.IRON_INGOT), 32)
                    )
                    , ItemsUtil.createItemLoreComponent(Material.ANCIENT_DEBRIS, "Make Vault Bastion", 1, ItemsUtil.getVaultLore()), 10));

            addRecipe(new FactoryRecipe("prisonpearl", "Make Prison Pearl", Arrays.asList(new ItemStack(Material.ENDER_PEARL)), Arrays.asList(new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.OBSIDIAN), 4), new ItemManager.ItemStorage(CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(Material.DIAMOND), 1)), ItemsUtil.createItem(Material.ENDER_PEARL, "Prison Pearl", 1), 10));

        }
    }
}
