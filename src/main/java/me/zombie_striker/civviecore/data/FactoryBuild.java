package me.zombie_striker.civviecore.data;

import me.zombie_striker.civviecore.managers.FactoryManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Furnace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FactoryBuild {

    private final Location craftingTable;
    private final Location furnace;
    private final Location chest;

    private final FactoryManager.FactoryType type;

    private FactoryRecipe currentRecipe;
    private int recipeTick = 0;
    private boolean running = false;


    public FactoryBuild(Location craftingTable, Location furnace, Location chest, FactoryManager.FactoryType type) {
        this.craftingTable = craftingTable;
        this.furnace = furnace;
        this.chest = chest;
        this.type = type;
    }

    public FactoryManager.FactoryType getType() {
        return type;
    }

    public Location[] getBlockLocations() {
        return new Location[]{craftingTable, furnace, chest};
    }

    public Location getChest() {
        return chest;
    }

    public Location getCraftingTable() {
        return craftingTable;
    }

    public Location getFurnace() {
        return furnace;
    }

    public FactoryRecipe getCurrentRecipe() {
        return currentRecipe;
    }

    public void setCurrentRecipe(FactoryRecipe fr) {
        this.currentRecipe = fr;
    }

    public int getRecipeTick() {
        return recipeTick;
    }

    public void setRecipeTick(int recipeTick) {
        this.recipeTick = recipeTick;
    }

    public void setRunning(boolean b) {
        this.running = b;
        if (furnace.getBlock().getBlockData() instanceof Furnace) {
            Furnace furnace1 = (Furnace) furnace.getBlock().getBlockData();
            furnace1.setLit(b);
            furnace.getBlock().setBlockData(furnace1);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void tick() {
        if (currentRecipe != null) {
            if (chest.getBlock().getState() instanceof Container) {
                Container chest1 = ((Container) chest.getBlock().getState());
                Inventory inv = chest1.getInventory();
                if (ItemsUtil.containsItemStorage(currentRecipe.getIngredients(), inv)) {
                    if (getCurrentRecipe().removeCoal()) {
                        for (int slot = 0; slot < inv.getSize(); slot++) {
                            ItemStack is = inv.getItem(slot);
                            if (is != null)
                                if (is.getType() == Material.CHARCOAL) {
                                    if (is.getAmount() > 1) {
                                        is.setAmount(is.getAmount() - 1);
                                        inv.setItem(slot, is);
                                    } else {
                                        inv.setItem(slot, null);
                                    }
                                    break;
                                }
                        }
                    }
                    if (getRecipeTick() < currentRecipe.getTickTime()) {
                        setRecipeTick(getRecipeTick() + 1);
                    } else {
                        getCurrentRecipe().produceResult(inv);
                    }
                } else {
                    setRunning(false);
                }
            }
        }
    }
}
