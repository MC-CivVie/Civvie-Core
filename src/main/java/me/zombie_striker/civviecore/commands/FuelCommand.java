package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FuelCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player){
            if(args.length==0){
                sender.sendMessage("Currently have: "+ CivvieAPI.getInstance().getFuelManager().getFuel(((Player) sender).getUniqueId()));
                sender.sendMessage("Usage: /fuel <amount> - to get fuel as items");
                return true;
            }
            int amount=0;
            try {
                amount = Integer.parseInt(args[0]);
            }catch (Exception e4){
                sender.sendMessage(args[0]+" is not an amount.");
                return true;
            }
            if(amount>0){
                if(amount > 64){
                    amount = 64;
                }
                ItemStack is = ItemsUtil.createFuel(amount);
                if(((Player) sender).getInventory().firstEmpty()==-1){
                    ((Player) sender).getLocation().getWorld().dropItem(((Player) sender).getLocation(),is);
                }else{
                    ((Player) sender).getInventory().addItem(is);
                }
                sender.sendMessage("Giving you "+amount+" Fuel Apples Items.");
            }
        }
        return true;
    }
}
