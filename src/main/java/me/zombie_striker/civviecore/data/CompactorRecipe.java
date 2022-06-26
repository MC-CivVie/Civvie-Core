package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.managers.ItemManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class CompactorRecipe extends FactoryRecipe{

    public CompactorRecipe(String name, ItemStack icon, int tickTime) {
        super(name, null,null, icon, tickTime);
    }

    @Override
    public void produceResult(Inventory inv) {
        for(int i = 0; i < inv.getSize(); i++){
            ItemStack is = inv.getItem(i);
            if(is!=null && is.getAmount()==is.getType().getMaxStackSize()){
                if(!ItemsUtil.isCompactedStack(is)) {
                    ItemStack compacted = is.clone();
                    compacted.setAmount(1);
                    List<Component> lore = new LinkedList<>();
                    lore.add(Component.text(ItemsUtil.COMPACTED_ITEM));
                    lore.addAll(is.lore());
                    compacted.lore(lore);
                    inv.setItem(i,null);
                    inv.addItem(compacted);
                }
            }
        }
    }
    public boolean removeCoal() {
        return false;
    }
}
