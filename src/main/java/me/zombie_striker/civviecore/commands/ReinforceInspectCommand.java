package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.managers.PlayerStateManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReinforceInspectCommand implements CommandExecutor, @Nullable TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            List<PlayerStateManager.PlayerState> playerState = CivvieAPI.getInstance().getPlayerStateManager().getPlayerStatesOf(player.getUniqueId(), PlayerStateManager.InspectReinforecePlayerState.class);
            if (playerState.size() > 0) {
                for (PlayerStateManager.PlayerState ps1 : playerState)
                    CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(ps1);
                player.sendMessage("Block Reinforcement Inspecting Disabled.");
            } else {
                PlayerStateManager.InspectReinforecePlayerState state = new PlayerStateManager.InspectReinforecePlayerState(player.getUniqueId());
                CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(state);
                player.sendMessage("Block Reinforcement Inspecting Enabled.");
            }

        }

        return true;
    }
}
