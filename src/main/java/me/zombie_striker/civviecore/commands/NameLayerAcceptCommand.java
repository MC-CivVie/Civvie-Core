package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import me.zombie_striker.civviecore.managers.PlayerStateManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NameLayerAcceptCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            return true;
        }
        Player player = (Player) sender;
        for(PlayerStateManager.PlayerState sentto : CivvieAPI.getInstance().getPlayerStateManager().getPlayerStatesOf(player.getUniqueId(),PlayerStateManager.InviteSentToPlayerState.class)){
            ((PlayerStateManager.InviteSentToPlayerState)sentto).getNameLayer().getRanks().put(QuickPlayerData.getPlayerData(player.getUniqueId()),((PlayerStateManager.InviteSentToPlayerState) sentto).getInvitedRank());
            for(QuickPlayerData quickPlayerData : ((PlayerStateManager.InviteSentToPlayerState) sentto).getNameLayer().getRanks().keySet()){
                Player player2 = Bukkit.getPlayer(quickPlayerData.getUuid());
                if(player2 !=null){
                    player2.sendMessage(Component.text("\""+player.getName()+"\" has accepted the invite to \""+((PlayerStateManager.InviteSentToPlayerState) sentto).getNameLayer().getName()+"\" as a(n) "+((PlayerStateManager.InviteSentToPlayerState) sentto).getInvitedRank().name()+"."));
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return null;
    }
}
