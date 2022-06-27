package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.managers.FactoryManager;
import me.zombie_striker.civviecore.managers.ItemManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import me.zombie_striker.ezinventory.EZGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FactoryModCommand implements CommandExecutor, @Nullable TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 27, "Factory Mod"));

        int slot2 = 0;
        for(FactoryManager.FactoryType factoryType : CivvieAPI.getInstance().getFactoryManager().getTypes()){
            ezgui.addCallable(ItemsUtil.createItem(factoryType.getIconMaterial(),factoryType.getDisplayname(),1,ItemsUtil.stringifyListItemStorage(factoryType.getIngredients())),(player, slot, isShiftClick, isRightClick) -> {
                player.sendMessage("Recipe for "+factoryType.getDisplayname()+": ");
                for(ItemManager.ItemStorage storage: factoryType.getIngredients()){
                    player.sendMessage("- "+storage.getItemType().getName()+" x "+storage.getAmount());
                }
                player.sendMessage("-----------------------------");
            },slot2);
            slot2++;
        }

        ((Player)sender).openInventory(ezgui.getInventory());

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
