package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.util.ItemsUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class DecompactorRecipe extends FactoryRecipe {

    public DecompactorRecipe(String name, ItemStack icon, int tickTime) {
        super(name, null, null, icon, tickTime);
    }


    @Override
    public void produceResult(Inventory inv) {
        if(inv.firstEmpty()!=-1) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack is = inv.getItem(i);
                if (is != null) {
                    if (ItemsUtil.isCompactedStack(is)) {
                        ItemStack compacted = is.clone();
                        compacted.setAmount(compacted.getType().getMaxStackSize());
                        List<Component> lore = is.lore();
                        for(int j = 0; j < lore.size(); j++){
                            Component t = lore.get(j);
                            if(t.toString().equals(ItemsUtil.COMPACTED_ITEM)){
                                lore.remove(t);
                            }
                        }
                        compacted.lore(lore);


                        if(is.getAmount()>1){
                            is.setAmount(is.getAmount()-1);
                            inv.setItem(i,is);
                        }else{
                            inv.setItem(i,null);
                        }

                        inv.addItem(compacted);
                    }
                }
            }
        }
    }
    public boolean removeCoal() {
        return false;
    }
}
