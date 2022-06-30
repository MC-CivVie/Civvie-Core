package me.zombie_striker.civviecore.commands;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.NameLayerRankEnum;
import me.zombie_striker.civviecore.data.QuickPlayerData;
import me.zombie_striker.civviecore.managers.PlayerStateManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import me.zombie_striker.ezinventory.EZGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NameLayerCommand implements CommandExecutor, TabCompleter {


    private NameLayerRankEnum[] order = {NameLayerRankEnum.OWNER, NameLayerRankEnum.ADMIN, NameLayerRankEnum.MANAGER, NameLayerRankEnum.MEMBER, NameLayerRankEnum.GUEST};

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
        }
        createGUI((Player) sender);
        return true;
    }

    private void createGUI(Player sender) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 54, "NameLayer Selection"));
        int slot = 0;

        for (NameLayerRankEnum rank : order) {
            for (NameLayer nameLayer : CivvieAPI.getInstance().getValidNameLayers()) {
                if (nameLayer.getRanks().containsKey(QuickPlayerData.getPlayerData(sender.getUniqueId())) && nameLayer.getRanks().get(QuickPlayerData.getPlayerData(sender.getUniqueId())) == rank) {
                    ezgui.addCallable(createNameLayerIcon(nameLayer, nameLayer.getRanks().get(QuickPlayerData.getPlayerData(sender.getUniqueId()))), (player, slot1, isShiftClick, isRightClick) -> {
                        createGUIEditor(sender, nameLayer);
                    }, slot);
                    slot++;
                }
            }
        }
        sender.openInventory(ezgui.getInventory());
    }

    public void createGUIEditor(Player sender, NameLayer nameLayer) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 54, nameLayer.getName() + " settings"));

        ezgui.addCallable(ItemsUtil.createItem(Material.COOKIE, "Invite Player", 1), (player, slot, isShiftClick, isRightClick) -> {
            createInviteGUI(sender, nameLayer);
        }, 0);
        ezgui.addCallable(ItemsUtil.createItem(Material.IRON_DOOR, "Leave Group", 1), (player, slot, isShiftClick, isRightClick) -> {

            if (nameLayer.getRanks().get(QuickPlayerData.getPlayerData(sender.getUniqueId())) == NameLayerRankEnum.OWNER) {
                boolean fallback = false;
                for (Map.Entry<QuickPlayerData, NameLayerRankEnum> e : nameLayer.getRanks().entrySet()) {
                    if (!e.getKey().getUuid().equals(sender.getUniqueId()) && e.getValue() == NameLayerRankEnum.OWNER) {
                        fallback = true;
                        break;
                    }
                }
                if (!fallback) {
                    sender.sendMessage("You cannot leave the group you are the only owner of!");
                    return;
                }
            }


            nameLayer.getRanks().remove(QuickPlayerData.getPlayerData(sender.getUniqueId()));
            sender.closeInventory();
            sender.sendMessage("You have left the group \"" + nameLayer.getName() + "\"");
        }, 3);


        int slotMembers = 9;
        for (NameLayerRankEnum ranks : order) {
            for (Map.Entry<QuickPlayerData, NameLayerRankEnum> e : nameLayer.getRanks().entrySet()) {
                if (e.getValue() == ranks) {
                    ezgui.addCallable(ItemsUtil.createItem(getMaterialByRank(ranks), e.getKey().getLastName(), 1), (player, slot, isShiftClick, isRightClick) -> {
                        createRankChangeInventory(player, nameLayer, e.getKey());
                    }, slotMembers);
                    slotMembers++;
                }
            }
        }


        sender.openInventory(ezgui.getInventory());
    }

    public void createRankChangeInventory(Player player, NameLayer nameLayer, QuickPlayerData who) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 18, "Select a new rank for " + who.getLastName()));

        createButtonForPromotion(player, nameLayer, who, ezgui, 1, NameLayerRankEnum.GUEST);
        createButtonForPromotion(player, nameLayer, who, ezgui, 2, NameLayerRankEnum.MEMBER);
        createButtonForPromotion(player, nameLayer, who, ezgui, 4, NameLayerRankEnum.MANAGER);
        createButtonForPromotion(player, nameLayer, who, ezgui, 6, NameLayerRankEnum.ADMIN);
        createButtonForPromotion(player, nameLayer, who, ezgui, 8, NameLayerRankEnum.OWNER);

        createButtonForPromotion(player, nameLayer, who, ezgui, 13, null);

        player.openInventory(ezgui.getInventory());
    }

    public void createButtonForPromotion(Player player, NameLayer nameLayer, QuickPlayerData who, EZGUI ezgui, int slot2, NameLayerRankEnum newRank) {

        if (newRank == null) {
            ezgui.addCallable(ItemsUtil.createItem(Material.BARRIER, "Kick from NameLayer", 1), (player2, slot, isShiftClick, isRightClick) -> {
                nameLayer.getRanks().remove(who);
                createGUIEditor(player, nameLayer);
            }, slot2);
            return;
        }


        //TODO: Add permission based checklist for if players can change rank.
        ezgui.addCallable(ItemsUtil.createItem(getMaterialByRank(newRank), newRank.name(), 1), (player2, slot, isShiftClick, isRightClick) -> {
            nameLayer.getRanks().put(who, newRank);
            createGUIEditor(player, nameLayer);
        }, slot2);
    }

    public void createInviteGUI(Player sender, NameLayer nameLayer) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 9, "What rank do you wish to give this player?"));

        callableInviteRank(ItemsUtil.createItem(Material.LEATHER_CHESTPLATE, NameLayerRankEnum.GUEST.name(), 1), nameLayer, NameLayerRankEnum.GUEST, sender, ezgui, 0);
        callableInviteRank(ItemsUtil.createItem(Material.IRON_CHESTPLATE, NameLayerRankEnum.MEMBER.name(), 1), nameLayer, NameLayerRankEnum.MEMBER, sender, ezgui, 2);
        callableInviteRank(ItemsUtil.createItem(Material.GOLDEN_CHESTPLATE, NameLayerRankEnum.MANAGER.name(), 1), nameLayer, NameLayerRankEnum.MANAGER, sender, ezgui, 4);
        callableInviteRank(ItemsUtil.createItem(Material.DIAMOND_CHESTPLATE, NameLayerRankEnum.ADMIN.name(), 1), nameLayer, NameLayerRankEnum.ADMIN, sender, ezgui, 6);
        callableInviteRank(ItemsUtil.createItem(Material.NETHERITE_CHESTPLATE, NameLayerRankEnum.OWNER.name(), 1), nameLayer, NameLayerRankEnum.OWNER, sender, ezgui, 8);

        sender.openInventory(ezgui.getInventory());
    }

    public void callableInviteRank(ItemStack icon, NameLayer nameLayer, NameLayerRankEnum rank, Player sender, EZGUI ezgui, int slot2) {

        ezgui.addCallable(icon, (player, slot, isShiftClick, isRightClick) -> {
            List<PlayerStateManager.PlayerState> playerstates = CivvieAPI.getInstance().getPlayerStateManager().getPlayerStates(sender.getUniqueId());
            if (!playerstates.isEmpty()) {
                for (PlayerStateManager.PlayerState ps : playerstates) {
                    if (ps instanceof PlayerStateManager.InviteMemberPlayerChatState) {
                        CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(ps);
                    }
                }
            }
            PlayerStateManager.InviteMemberPlayerChatState invite = new PlayerStateManager.InviteMemberPlayerChatState(sender.getUniqueId(), nameLayer, rank);
            CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(invite);
            sender.sendMessage("Please type the name of the person you wish to invite.");
            sender.closeInventory();
        }, slot2);

    }

    public Material getMaterialByRank(NameLayerRankEnum rankEnum) {
        Material mat = null;
        switch (rankEnum) {
            case OWNER:
                mat = Material.NETHERITE_CHESTPLATE;
                break;
            case ADMIN:
                mat = Material.DIAMOND_CHESTPLATE;
                break;
            case MANAGER:
                mat = Material.GOLDEN_CHESTPLATE;
                break;
            case MEMBER:
                mat = Material.IRON_CHESTPLATE;
                break;
            case GUEST:
                mat = Material.LEATHER_CHESTPLATE;
                break;
        }
        return mat;
    }


    public ItemStack createNameLayerIcon(NameLayer nameLayer, NameLayerRankEnum rankOfPlayer) {
        Material mat = getMaterialByRank(rankOfPlayer);

        return ItemsUtil.createItem(mat, nameLayer.getName(), 1);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new LinkedList<>();
    }
}
