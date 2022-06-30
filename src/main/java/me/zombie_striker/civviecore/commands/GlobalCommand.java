package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.managers.PlayerStateManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class GlobalCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        Player player = (Player) sender;
        PlayerStateManager.NameLayerChatState state = (PlayerStateManager.NameLayerChatState) CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(((Player) sender).getUniqueId(), PlayerStateManager.NameLayerChatState.class);
        if (args.length == 0) {
            if (state == null) {
                sender.sendMessage("Usage: /g <namelayer> or /g !");
                return true;
            } else {
                CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(state);
                sender.sendMessage(Component.text("Now chatting in \"[!]\"").color(TextColor.color(10, 200, 100)));
                return true;
            }
        }
        String arg = args[0];
        if (arg.equals("!")) {
            CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(state);
            sender.sendMessage(Component.text("Now chatting in \"[!]\"").color(TextColor.color(10, 200, 100)));
        } else {
            NameLayer nl = CivvieAPI.getInstance().getNameLayerCalled(((Player) sender).getUniqueId(), arg);
            if (nl == null) {
                sender.sendMessage(Component.text("You are not part of any group called \"" + arg + "\".").color(TextColor.color(200, 10, 10)));
                return true;
            }
            CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(((Player) sender).getUniqueId(), PlayerStateManager.NameLayerChatState.class));
            CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(new PlayerStateManager.NameLayerChatState(((Player) sender).getUniqueId(), nl));
            sender.sendMessage(Component.text("Now chatting in \"[" + nl.getName() + "]\"").color(TextColor.color(10, 200, 100)));

        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> strings = new LinkedList<>();
        strings.add("!");
        for(NameLayer nameLayer : CivvieAPI.getInstance().getNameLayersFor(((Player)sender).getUniqueId())){
            strings.add(nameLayer.getName());
        }
        return strings;
    }
}
