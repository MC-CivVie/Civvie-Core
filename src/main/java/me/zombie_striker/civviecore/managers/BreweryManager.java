package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.util.ItemsUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class BreweryManager {

    private Brew failed = new Brew("failed", "Failed Drink", Color.OLIVE,0,0);
    private Brew water = new Brew("water", "Water", Color.AQUA,0,0);

    private List<Brew> brews = new LinkedList<>();

    private HashMap<Location, List<ItemManager.ItemType>> worts = new HashMap<>();
    private HashMap<Location, Long> brewtimes = new HashMap<>();

    private List<ItemManager.ItemType> validingredients = new LinkedList<>();

    public BreweryManager() {

    }

    public void init(CivvieCorePlugin plugin) {
        File folderRecipes = new File(plugin.getDataFolder(), "brews");
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

                int min = config.getInt("maxcooktime");
                int max = config.getInt("mincooktime");

                int red = config.getInt("color.red");
                int green = config.getInt("color.green");
                int blue = config.getInt("color.blue");
                Brew brew = new Brew(name, displayname, Color.fromBGR(red, green, blue),min,max);
                for (ItemManager.ItemStorage is : itemsIngreidents) {
                    brew.addIngredient(is);
                    if (!validingredients.contains(is.getItemType())) {
                        validingredients.add(is.getItemType());
                    }
                }
                brews.add(brew);
            }
        }
    }

    public List<ItemManager.ItemType> getValidIngredients() {
        return validingredients;
    }

    public HashMap<Location, List<ItemManager.ItemType>> getWorts() {
        return worts;
    }

    public HashMap<Location, Long> getBrewtimes() {
        return brewtimes;
    }

    public List<Brew> getBrews() {
        return brews;
    }

    public void addIngredientToWort(Location location, ItemManager.ItemType type) {
        if (worts.containsKey(location)) {
            worts.get(location).add(type);
            return;
        }
        List<ItemManager.ItemType> ings = new LinkedList<>();
        ings.add(type);
        worts.put(location, ings);
        brewtimes.put(location, System.currentTimeMillis());
    }

    public Brew getBrew(Block relative) {
        if (relative.getState() instanceof Container) {
            Container container = (Container) relative.getState();
            ItemStack firstBrew = null;

            int slot = -1;

            for (int slots = 0; slots < container.getInventory().getSize();slots++) {
                ItemStack is = container.getInventory().getItem(slots);
                if (is!=null&&is.getType() == Material.WATER_BUCKET) {
                    firstBrew = is;
                    slot=slots;
                    break;
                }
            }

            if (firstBrew == null)
                return null;

            List<ItemManager.ItemType> ingredients = new LinkedList<>();
            int percent = 100;
            int cooktime = 1;

            if (firstBrew.hasItemMeta() && firstBrew.getItemMeta().hasLore()) {
                List<String> lores = firstBrew.getLore();
                for (String lore : lores) {
                    if (lore.contains("Brewing Time:")) {
                        cooktime = Integer.parseInt(lore.split(": ")[1]);
                    }
                    if (lore.contains("Percent Full:")) {
                        percent = Integer.parseInt(lore.split(": ")[1]);
                    }
                    if (lore.startsWith("-")) {
                        ingredients.add(CivvieAPI.getInstance().getItemManager().getItemTypeByName(lore.substring(1)));
                    }
                }
            }

            if (ingredients.size() == 0) {
                return water;
            }

            percent-=20;

            if(percent<0){
                container.getInventory().setItem(slot,new ItemStack(Material.BUCKET));
            }else {
                ItemStack newwort = firstBrew.clone();
                List<Component> loressz = newwort.getItemMeta().lore();
                loressz.set(0, Component.text("Percent Full: " + percent));
                ItemMeta im = newwort.getItemMeta();
                im.lore(loressz);
                newwort.setItemMeta(im);
                container.getInventory().setItem(slot,newwort);
            }


            for (Brew brew : brews) {
                if (ingredients.size() == brew.getIngredients().size()) {
                    boolean[] checklist = new boolean[ingredients.size()];
                    for (int i = 0; i < ingredients.size(); i++) {
                        ItemManager.ItemType type = ingredients.get(i);
                        for (int j = 0; j < brew.ingredients.size(); j++) {
                            ItemManager.ItemType typej = brew.getIngredients().get(j).getItemType();
                            if (type == typej) {
                                if (!checklist[i]) {
                                    checklist[i] = true;
                                    break;
                                }
                            }
                        }
                    }


                    boolean all = true;
                    for(boolean b : checklist){
                        if(!b){
                            all=false;
                            break;
                        }
                    }
                    if(all){
                        if(brew.getMinCookTime()<=cooktime&&brew.getMaxCookTime()>=cooktime){
                            return brew;
                        }
                    }
                }
            }
        }
        return null;
    }

    public Brew getFailed() {
        return failed;
    }

    public Brew getWater() {
        return water;
    }

    public class Brew {
        private String name;
        private String displayName;
        private List<ItemManager.ItemStorage> ingredients = new LinkedList<>();
        private Color color;

        private int minCookTime;
        private int maxCookTime;

        public Brew(String name, String displayName, Color color, int minCookTime, int maxCookTime) {
            this.name = name;
            this.displayName = displayName;
            this.color = color;
            this.minCookTime = minCookTime;
            this.maxCookTime = maxCookTime;
        }

        public int getMaxCookTime() {
            return maxCookTime;
        }

        public int getMinCookTime() {
            return minCookTime;
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

        public void addIngredient(ItemManager.ItemStorage storage) {
            this.ingredients.add(storage);
        }
    }
}
