package me.zombie_striker.civviecore.enchantments;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GenericWeaponEnchant extends GenericEnchant {
    @Override
    public boolean canApplyTo(ItemStack is) {
        return is.getType().name().endsWith("SWORD")||
                is.getType().name().endsWith("_AXE");
    }
    public GenericWeaponEnchant(String name, String lore, @NotNull NamespacedKey key) {
        super(name, lore, key);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }
}
