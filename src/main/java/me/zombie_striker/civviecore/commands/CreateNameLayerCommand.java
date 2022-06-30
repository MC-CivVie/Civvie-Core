package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.NameLayerRankEnum;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreateNameLayerCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0){
            sender.sendMessage("Please provide the name for the NameLayer you wish to create (must be unique).");
            return true;
        }
        String name = args[0];
        if(name.equals("!")){
            sender.sendMessage("This name has already been taken.");
            return true;
        }
        for(NameLayer nameLayer : CivvieAPI.getInstance().getValidNameLayers()){
            if(nameLayer.getName().equals(name)){
                sender.sendMessage("This name has already been taken.");
                return true;
            }
        }
        NameLayer namelayer = new NameLayer(name);
        if(!(sender instanceof Player)){
            sender.sendMessage("Only players can run this command.");
            return true;
        }
        namelayer.getRanks().put(QuickPlayerData.getPlayerData(((Player) sender).getUniqueId()), NameLayerRankEnum.OWNER);
        CivvieAPI.getInstance().registerNameLayer(namelayer);
        sender.sendMessage(Component.text("Successfully created NameLayer \""+name+"\"").color(TextColor.color(100,200,100)));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
