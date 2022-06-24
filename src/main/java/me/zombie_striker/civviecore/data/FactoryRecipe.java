package me.zombie_striker.civviecore.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FactoryRecipe {

    private List<ItemStack> ingredients;
    private List<ItemStack> results;
    private String name;

    public FactoryRecipe(String name, List<ItemStack> results, List<ItemStack> ingredients){
        this.name = name;
        this.results = results;
        this.ingredients = ingredients;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getResults() {
        return results;
    }

    public String getName() {
        return name;
    }
}
