package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.data.NameLayer;
import me.zombie_striker.civviecore.data.NameLayerRankEnum;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.naming.Name;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class PlayerStateManager {

    private List<PlayerState> playerstates = new LinkedList<>();

    public List<PlayerState> getPlayerStates(UUID uuid){
        List<PlayerState> playerStates = new LinkedList<>();
        for(PlayerState playerState : playerstates){
            if(playerState.getUuid().equals(uuid))
                playerStates.add((playerState));
        }
        return playerStates;
    }
    public List<PlayerState> getPlayerStatesOf(UUID uuid, Class<? extends PlayerState> type){
        List<PlayerState> playerStates = new LinkedList<>();
        for(PlayerState playerState : playerstates){
            if(playerState.getUuid().equals(uuid))
                if(type.isInstance(playerState)) {
                    playerStates.add((playerState));
                }
        }
        return playerStates;
    }


    public PlayerState getPlayerStateOf(UUID uuid, Class<? extends PlayerState> type){
        for(PlayerState playerState : playerstates){
            if(playerState.getUuid().equals(uuid))
                if(type.isInstance(playerState)){
                    return playerState;
                }
        }
        return null;
    }

    public void removePlayerState(PlayerState playerState){
        this.playerstates.remove(playerState);
    }

    public List<PlayerState> getPlayerstates() {
        return playerstates;
    }
    public void addPlayerState(PlayerState playerState){
        this.playerstates.add(playerState);
    }

    public static class PlayerState{

        private UUID uuid;

        public PlayerState(UUID uuid){
            this.uuid = uuid;
        }

        public UUID getUuid() {
            return uuid;
        }
    }
    public static class InviteMemberPlayerChatState extends PlayerState{

        private NameLayerRankEnum invitedRank;
        public InviteMemberPlayerChatState( UUID uuid, NameLayerRankEnum inviteRank) {
            super(uuid);
            this.invitedRank = inviteRank;
        }

        public NameLayerRankEnum getInvitedRank() {
            return invitedRank;
        }
    }

    public static class ReinforceBlockState extends PlayerState{

        private Material reinforce;
        private NameLayer reinforceTo;

        public ReinforceBlockState(UUID uuid, NameLayer reinforceTo, Material reinforce) {
            super(uuid);
            this.reinforce = reinforce;
            this.reinforceTo = reinforceTo;
        }

        public Material getReinforce() {
            return reinforce;
        }

        public NameLayer getReinforceTo() {
            return reinforceTo;
        }
    }
    public static class TriggerMoveJukeAlertState extends PlayerState{
        private Location jukebox;

        public TriggerMoveJukeAlertState(UUID uuid, Location jukebox) {
            super(uuid);
            this.jukebox = jukebox;
        }

        public Location getJukebox() {
            return jukebox;
        }
    }
}
