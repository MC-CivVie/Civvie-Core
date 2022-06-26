package me.zombie_striker.civviecore.util;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.managers.ItemManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedList;
import java.util.List;

public class ItemsUtil {

    public static final String PRISON_PEARL_NAME = "Prison Pearl (%name%)";
    public static final String COMPACTED_ITEM = "Compacted Item";

    public static List<ItemStack> stringListToItemStackList(List<String> strings) {
        List<ItemStack> result = new LinkedList<>();
        for (String s : strings) {
            String[] split = s.split("\\,");
            Material material = Material.matchMaterial(split[0]);
            int amount = 1;
            if (split.length > 1) {
                amount = Integer.parseInt(split[1]);
            }
            ItemStack is = new ItemStack(material, amount);
            result.add(is);
        }
        return result;
    }

    public static boolean containsItems(List<ItemStack> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for (int stack = 0; stack < stacks.size(); stack++) {
            b[stack] = stacks.get(stack).getAmount();
        }


        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is != null) {
                for (int i = 0; i < stacks.size(); i++) {
                    ItemStack ii = stacks.get(i);
                    if (b[i] > 0 && ii.getType() == is.getType()) {
                        if (b[i] <= is.getAmount()) {
                            b[i] = 0;
                        } else {
                            b[i] = b[i] - is.getAmount();
                        }
                    }
                }
            }
        }

        for (int i : b) {
            if (i > 0)
                return false;
        }
        return true;
    }

    public static ItemStack createItem(Material material, String name, int amount, String... lore) {
        ItemStack is = new ItemStack(material, amount);
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(name));
        List<Component> loreC = new LinkedList<>();
        for (String l : lore) {
            loreC.add(Component.text(l));
        }
        im.lore(loreC);
        is.setItemMeta(im);
        return is;
    }

    public static List<ItemManager.ItemStorage> stringListToItemTypeList(List<String> strings) {
        List<ItemManager.ItemStorage> result = new LinkedList<>();
        for (String s : strings) {
            String[] split = s.split("\\,");
            ItemManager.ItemType it = CivCore.getInstance().getItemManager().getItemTypeByName(s);
            int amount = 1;
            if (split.length > 1) {
                amount = Integer.parseInt(split[1]);
            }
            result.add(new ItemManager.ItemStorage(it, amount));
        }
        return result;
    }

    public static boolean containsItemStorage(List<ItemManager.ItemStorage> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for (int stack = 0; stack < stacks.size(); stack++) {
            b[stack] = stacks.get(stack).getAmount();
        }


        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is != null) {
                for (int i = 0; i < stacks.size(); i++) {
                    ItemManager.ItemStorage ii = stacks.get(i);
                    if (b[i] > 0 && ii.getItemType().isType(is)) {
                        if (b[i] <= is.getAmount()) {
                            b[i] = 0;
                        } else {
                            b[i] = b[i] - is.getAmount();
                        }
                    }
                }
            }
        }

        for (int i : b) {
            if (i > 0)
                return false;
        }
        return true;
    }

    public static void removeItemStorage(List<ItemManager.ItemStorage> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for (int stack = 0; stack < stacks.size(); stack++) {
            b[stack] = stacks.get(stack).getAmount();
        }


        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is != null) {
                for (int i = 0; i < stacks.size(); i++) {
                    ItemManager.ItemStorage ii = stacks.get(i);
                    if (b[i] > 0 && ii.getItemType().isType(is)) {
                        if (b[i] < is.getAmount()) {
                            is.setAmount(is.getAmount() - b[i]);
                            b[i] = 0;
                            inventory.setItem(slot, is);
                        } else if (b[i] == is.getAmount()) {
                            b[i] = 0;
                            inventory.setItem(slot, null);
                        } else {
                            b[i] = b[i] - is.getAmount();
                            inventory.setItem(slot, null);
                        }
                    }
                }
            }
        }
    }

    public static void removeItems(List<ItemStack> stacks, Inventory inventory) {
        int[] b = new int[stacks.size()];

        for (int stack = 0; stack < stacks.size(); stack++) {
            b[stack] = stacks.get(stack).getAmount();
        }


        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is != null) {
                for (int i = 0; i < stacks.size(); i++) {
                    ItemStack ii = stacks.get(i);
                    if (b[i] > 0 && ii.getType() == is.getType()) {
                        if (b[i] < is.getAmount()) {
                            is.setAmount(is.getAmount() - b[i]);
                            b[i] = 0;
                            inventory.setItem(slot, is);
                        } else if (b[i] == is.getAmount()) {
                            b[i] = 0;
                            inventory.setItem(slot, null);
                        } else {
                            b[i] = b[i] - is.getAmount();
                            inventory.setItem(slot, null);
                        }
                    }
                }
            }
        }
    }

    public static boolean isPrisonPearl(ItemStack is) {
        if (is.getType() != Material.ENDER_PEARL)
            return false;
        if (is.displayName().toString().startsWith("Prison Pearl"))
            return true;
        return false;
    }

    public static boolean isCompactedStack(ItemStack is) {
        if (is.getItemMeta().hasLore()) {
            for (Component c : is.getItemMeta().lore()) {
                if (c.toString().equals(COMPACTED_ITEM)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack createPrisonPearl(OfflinePlayer player, OfflinePlayer killer, String datePearled, int health, String pearlcode) {
        ItemStack is = new ItemStack(Material.ENDER_PEARL);
        ItemMeta im = is.getItemMeta();
        im.displayName(Component.text(PRISON_PEARL_NAME.replaceAll("%name%", player.getName())));

        List<Component> lore = new LinkedList<>();
        lore.add(Component.text("Player: ").color(TextColor.color(200, 200, 10)).append(Component.text(player.getName()).color(TextColor.color(150, 150, 150))).append(Component.text(" #" + pearlcode).color(TextColor.color(50, 50, 50))));
        lore.add(Component.text("Health: ").color(TextColor.color(200, 200, 10)).append(Component.text(health).color(TextColor.color(150, 150, 150))));
        lore.add(Component.text("Date Killed: ").color(TextColor.color(200, 200, 10)).append(Component.text(datePearled).color(TextColor.color(150, 150, 150))));
        lore.add(Component.text("Killed By: ").color(TextColor.color(200, 200, 10)).append(Component.text(killer.getName()).color(TextColor.color(150, 150, 150))));
        lore.add(Component.text("Cost per week to maintain: ").color(TextColor.color(200, 200, 10)).append(Component.text("TBD").color(TextColor.color(150, 150, 150))));
        lore.add(Component.text("Upgrade Cost: ").color(TextColor.color(200, 200, 10)).append(Component.text("TBD").color(TextColor.color(150, 150, 150))));
        lore.add(Component.text(""));
        lore.add(Component.text("Commands:").color(TextColor.color(0, 200, 0)));
        lore.add(Component.text("/ep free").color(TextColor.color(50, 200, 200)).append(Component.text(" - Frees the player from their pearl.").color(TextColor.color(200, 150, 20))));
        im.lore(lore);
        is.setItemMeta(im);
        return is;
    }

    public static void removeItem(Material reinforce, int amount, Player player) {
        for(int i = 0; i < player.getInventory().getSize();i++){
            ItemStack is = player.getInventory().getItem(i);
            if(is!=null && is.getType() == reinforce){
                if(is.getAmount()>1){
                    is.setAmount(is.getAmount()-1);
                    player.getInventory().setItem(i,is);
                }else{
                   player.getInventory().setItem(i,null);
                }
                return;
            }
        }
    }
}
