package me.zombie_striker.civviecore.commands;

import com.sun.tools.javac.jvm.Items;
import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import me.zombie_striker.civviecore.managers.PearlManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RefuelCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player))
            return true;
        if(((Player) sender).getInventory().getItemInMainHand()!=null&& ItemsUtil.isPrisonPearl(((Player) sender).getInventory().getItemInMainHand())){
            int amount = CivvieAPI.getInstance().getFuelManager().getFuel(((Player) sender).getUniqueId());
            if(amount>0){
                PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(((Player) sender).getInventory().getItemInMainHand());

                int i = 7;
                if(args.length != 0){
                    i = Math.min(i,Integer.parseInt(args[0]));
                }
                if(i <= pearlData.getFuel()){
                    sender.sendMessage(Component.text("This pearl already is fueled for that many days!").color(TextColor.color(200,10,10)));
                    return true;
                }

                int refuelAmount = (int) Math.min(i-pearlData.getFuel(),amount);

                amount-=refuelAmount;

                pearlData.setLastRefuel(System.currentTimeMillis());
                pearlData.setFuel(pearlData.getFuel()+refuelAmount);

                ((Player) sender).getInventory().setItemInMainHand(ItemsUtil.createPrisonPearl(Bukkit.getOfflinePlayer(pearlData.getUuid()),Bukkit.getOfflinePlayer(pearlData.getKiller()),ItemsUtil.formatDate(pearlData.getTimeKilled()),ItemsUtil.formatDate(pearlData.getLastRefuel()),(int)(pearlData.getFuel()),pearlData.getDesignation()));
                CivvieAPI.getInstance().getFuelManager().setFuel(((Player) sender).getUniqueId(),amount);
                sender.sendMessage(Component.text(refuelAmount+" fuel spent refueling pearl for "+ Bukkit.getOfflinePlayer(pearlData.getUuid()).getName()+".").color(TextColor.color(200,200,180)));
            }
        }else{
            sender.sendMessage("Must have a Prison Pearl in your hand to fuel it.");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
