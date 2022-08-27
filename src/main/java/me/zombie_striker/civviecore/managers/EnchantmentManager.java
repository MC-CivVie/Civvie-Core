package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.enchantments.*;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EnchantmentManager {

    private List<GenericEnchant> validEnchantments = new LinkedList<>();


    /**
     * Fehu - Wealth
     * Uruz - Strength
     * Thurisaz - Protection
     * Ansuz - Sight - God
     * Raidho - Speed
     * Kaunaz - "Torch" - Purpose
     * Gebo - Gift
     * Wunjo - Success - joy
     * Hagalaz - "Hail" - Destruction
     * Nauthiz - "Needs"
     * Isa - Ice
     * Jara - Harvest
     * Eihwaz - Endurance
     * Perthro - Destiny
     * Algiz - Protection
     * Sowelo - "Sun" - Happiness
     * Tiwaz - "Victory"
     * Berkana - Renewal
     * Ehwaz - "Horse" - Movement
     * Mannaz - "Man"
     * Laguz - "Lake" - Healing
     * Ingus - Peace
     * Othila - Abundance - Values - possession
     * Dagaz - Hope - day
     *
     */

    public GenericEnchant fehu;
    public GenericEnchant uruz;
    public GenericEnchant thurisaz;
    public GenericEnchant ansuz;
    public GenericEnchant raidho;
    public GenericEnchant kaunaz;
    public GenericEnchant gebo;
    public GenericEnchant wunjo;
    public GenericEnchant hagalaz;
    public GenericEnchant nauthiz;
    public GenericEnchant isa;
    public GenericEnchant jara;
    public GenericEnchant eihwaz;
    public GenericEnchant perthro;
    public GenericEnchant algiz;
    public GenericEnchant sowelo;
    public GenericEnchant tiwaz;
    public GenericEnchant berkana;
    public GenericEnchant ehwaz;
    public GenericEnchant mannaz;
    public GenericEnchant laguz;
    public GenericEnchant ingus;
    public GenericEnchant orthila;
    public GenericEnchant dagaz;

    public EnchantmentManager(){

    }

    public void init(CivvieCorePlugin plugin){
        //Protection - Sword
        thurisaz = (GenericEnchant) registerEnchantment(new GenericArmorEnchant("Thurisaz", "Protection",new NamespacedKey(plugin,"civvie.ench1")));
        // Protection - Axe
        algiz = (GenericEnchant) registerEnchantment(new GenericArmorEnchant("Algiz", "Protection",new NamespacedKey(plugin,"civvie.ench2")));
        // Protection - Ice (Isa)
        sowelo = (GenericEnchant) registerEnchantment(new GenericArmorEnchant("Sowelo", "Sun",new NamespacedKey(plugin,"civvie.ench3")));
        // Protection - Arrow
        wunjo = (GenericEnchant) registerEnchantment(new GenericArmorEnchant("Wunjo", "Success",new NamespacedKey(plugin,"civvie.ench4")));
        // Protection - (Hagalaz)
        ingus = (GenericEnchant) registerEnchantment(new GenericArmorEnchant("Ingus","Peace", new NamespacedKey(plugin,"civvie.ench5")));

        //Sword/Axe - Sharpness
        uruz = (GenericEnchant) registerEnchantment(new GenericWeaponEnchant("Uruz", "Strength",new NamespacedKey(plugin,"civvie.ench6")));
        //Sword/Axe - Sharpness 2
        hagalaz = (GenericEnchant) registerEnchantment(new GenericWeaponEnchant("Hagalaz","Destruction", new NamespacedKey(plugin,"civvie.ench7")));
        //Sword/Axe - Ice 2
        isa = (GenericEnchant) registerEnchantment(new GenericWeaponEnchant("Isa", "Ice",new NamespacedKey(plugin,"civvie.ench8")));

        //Unbreaking
        laguz = (GenericEnchant) registerEnchantment(new GenericAllEnchant("Laguz", "Endurance",new NamespacedKey(plugin,"civvie.ench9")));

        raidho = (GenericEnchant) registerEnchantment(new GenericBootEnchant("Raidho","Journey", new NamespacedKey(plugin,"civvie.ench10")));

    }




    // ItemMeta#hasEnchant() may also work for this function
    public boolean hasEnchantment(ItemStack item, Enchantment enchant){
        if(item.getItemMeta() != null && item.getItemMeta().getEnchants() != null && item.getItemMeta().getEnchants().size() > 0){
            for (Iterator<Map.Entry<Enchantment, Integer>> it = item.getItemMeta().getEnchants().entrySet().iterator(); it.hasNext();) {
                java.util.Map.Entry<Enchantment, Integer> e = it.next();
                if(e.getKey().equals(enchant)){
                    return true;
                }
            }
        }
        return false;
    }
    // ItemMeta#getEnchantLevel() may also work for this function
    public int getLevel(ItemStack item, Enchantment enchant){
        if(item.getItemMeta() != null && item.getItemMeta().getEnchants() != null && item.getItemMeta().getEnchants().size() > 0){
            for (Iterator<java.util.Map.Entry<Enchantment, Integer>> it = item.getItemMeta().getEnchants().entrySet().iterator(); it.hasNext();) {
                java.util.Map.Entry<Enchantment, Integer> e = it.next();
                if(e.getKey().equals(enchant)){
                    return e.getValue();
                }
            }
        }
        return 0;
    }


    public List<GenericEnchant> getValidEnchantments() {
        return validEnchantments;
    }

    //Load custom enchantments
    public Enchantment registerEnchantment(Enchantment enchantment) {
        boolean registered = true;
        try {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
            Enchantment.registerEnchantment(enchantment);
        } catch (Exception e) {
            registered = false;
            e.printStackTrace();
        }
        if(registered){
            // It's been registered!
        }
        validEnchantments.add((GenericEnchant) enchantment);
        return enchantment;
    }


}
