package me.zombie_striker.civviecore.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GenericAllEnchant extends GenericEnchant {
    @Override
    public boolean canApplyTo(ItemStack is) {
        return is.getType().name().endsWith("SWORD")||
                is.getType().name().endsWith("SHOVEL")||
                is.getType().name().endsWith("_HOE")||
                is.getType().name().endsWith("_BOW")||
                is.getType().name().endsWith("CROSSBOW")||
                is.getType().name().endsWith("PICKAXE")||
                is.getType().name().endsWith("_AXE")||
                is.getType().name().endsWith("HELMET")||
                is.getType().name().endsWith("LEGGINGS")||
                is.getType().name().endsWith("CHESTPLATE")||
                is.getType().name().endsWith("BOOTS");
    }
    public GenericAllEnchant(String name, String lore, @NotNull NamespacedKey key) {
        super(name, lore, key);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }
}
