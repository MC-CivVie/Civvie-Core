package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.managers.FactoryManager;
import me.zombie_striker.ezinventory.EZGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class FactoryModCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 27, "Factory Mod"));

        for(FactoryManager.FactoryType factoryType : CivCore.getInstance().getFactoryManager().getTypes()){

        }

        return false;
    }
}
