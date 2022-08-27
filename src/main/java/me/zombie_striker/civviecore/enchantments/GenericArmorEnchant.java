package me.zombie_striker.civviecore.enchantments;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GenericArmorEnchant extends GenericEnchant {

    @Override
    public boolean canApplyTo(ItemStack is) {
        return is.getType().name().endsWith("HELMET")||
                is.getType().name().endsWith("LEGGINGS")||
                is.getType().name().endsWith("CHESTPLATE")||
                is.getType().name().endsWith("BOOTS");
    }

    public GenericArmorEnchant(String name, String lore, @NotNull NamespacedKey key) {
        super(name, lore, key);
    }

    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEARABLE;
    }
}
