package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivCore;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class ReinforceCommand implements CommandExecutor, TabExecutor {

    private CivvieCorePlugin  plugin;

    public ReinforceCommand(CivvieCorePlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 0){
            sender.sendMessage("You need to provide a NameLayer to reinforce to.");
            return true;
        }
        String namelayer = args[0];
        if(sender instanceof Player) {
            Player player = (Player) sender;
            NameLayer nl = CivCore.getInstance().getNameLayerCalled(player.getUniqueId(),namelayer);
            if(namelayer==null){
                player.sendMessage("You need to provide a valid NameLayer.");
                return true;
            }
            Material mat = Material.AIR;
            if(player.getInventory().getItemInMainHand()!=null){
                mat=  player.getInventory().getItemInMainHand().getType();
            }
            if(!CivCore.getInstance().getReinforceMaterial().containsKey(mat)){
                sender.sendMessage("You cannot reinforce blocks with "+mat.name()+".");
                return true;
            }

            CivCore.getInstance().getReinforcingTo().put(player.getUniqueId(),nl);
            CivCore.getInstance().getReinforceMaterial().put(player.getUniqueId(),mat);
            sender.sendMessage("You are now reinforcing to NameLayer "+nl.getName()+".");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> nls = new LinkedList<>();
        if (sender instanceof Player){
            Player player = (Player) sender;
            for (NameLayer nl : CivCore.getInstance().getValidNameLayers()) {
                for (QuickPlayerData qpd : nl.getRanks().keySet()) {
                    if (qpd.getUuid().equals(player.getUniqueId()))
                        nls.add(nl.getName());
                }
            }
        }
        return nls;
    }
}
