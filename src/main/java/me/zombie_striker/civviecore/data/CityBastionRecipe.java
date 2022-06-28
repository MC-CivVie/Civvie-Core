package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.managers.ItemManager;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CityBastionRecipe extends FactoryRecipe{

    public CityBastionRecipe(String name, String displayname, List<ItemStack> results, List<ItemManager.ItemStorage> ingredients, ItemStack icon, int tickTime) {
        super(name, displayname, results, ingredients, icon, tickTime);
    }
}
