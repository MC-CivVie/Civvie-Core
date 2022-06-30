package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.managers.PearlManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class EPCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            return true;
        }
        if(args.length==0){
            sender.sendMessage("Usage: /ep free - Frees the pearl you are holding.");
            sender.sendMessage("Usage: /ep locate - Locates the pearl that is holding you.");
            return true;
        }
        if(args[0].equals("free")){
            if(((Player) sender).getInventory().getItemInMainHand()!=null&&((Player) sender).getInventory().getItemInMainHand().getType()== Material.ENDER_PEARL){
                PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(((Player) sender).getInventory().getItemInMainHand());
                if(pearlData!=null){
                    CivvieAPI.getInstance().getPearlManager().freePearl(pearlData);
                    sender.sendMessage( Bukkit.getOfflinePlayer(pearlData.getUuid()).getName()+" has been freed!");
                    ((Player) sender).getInventory().setItemInMainHand(null);
                    return true;
                }else{
                    sender.sendMessage("Invalid pearl.");
                }
            }else{
                sender.sendMessage("You need to hold the pearl you wish to free in your main hand!");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length <= 1){
            List<String> list = new LinkedList<>();
            a(list,args[0],"free");
            return list;
        }
        return null;
    }
    private void a(List<String> list, String arg, String whattobe){
        if(whattobe.toLowerCase().startsWith(arg.toLowerCase()))
            list.add(whattobe);
    }
}
