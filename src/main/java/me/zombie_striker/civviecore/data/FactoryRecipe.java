package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class FactoryRecipe {

    private List<ItemManager.ItemStorage> ingredients;
    private List<ItemStack> results;
    private String name;
    private ItemStack icon;
    private int tickTime;

    public FactoryRecipe(String name, List<ItemStack> results, List<ItemManager.ItemStorage> ingredients, ItemStack icon, int tickTime){
        this.name = name;
        this.results = results;
        this.ingredients = ingredients;
        this.icon = icon;
        this.tickTime = tickTime;
    }

    public int getTickTime() {
        return tickTime;
    }

    public List<ItemManager.ItemStorage> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getResults() {
        return results;
    }

    public String getName() {
        return name;
    }

    public ItemStack getIcon() {
        return icon;
    }
}
