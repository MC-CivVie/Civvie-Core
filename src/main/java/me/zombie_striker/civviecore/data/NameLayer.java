package me.zombie_striker.civviecore.data;

import java.util.HashMap;
import java.util.UUID;

public class NameLayer {

    private HashMap<QuickPlayerData, NameLayerRankEnum> ranks = new HashMap<>();
    private HashMap<NameLayerPermissions,Boolean> permissionsMap = new HashMap<>();

    private String name;
    private UUID nlUUID;

    public NameLayer(String name){
        this.name = name;
        this.nlUUID = UUID.randomUUID();
        defaultPermMap();
    }
    public NameLayer(UUID uuid, String name){
        this.name = name;
        this.nlUUID = uuid;
        defaultPermMap();
    }
    private void defaultPermMap(){
        permissionsMap.put(NameLayerPermissions.GUEST_INVITE_USER,false);
        permissionsMap.put(NameLayerPermissions.MEMBER_INVITE_USER,false);
        permissionsMap.put(NameLayerPermissions.MODERATOR_INVITE_USER,true);
        permissionsMap.put(NameLayerPermissions.ADMIN_INVITE_USER,true);
        permissionsMap.put(NameLayerPermissions.OWNER_INVITE_USER,true);

    }

    public String getName() {
        return name;
    }

    public UUID getNlUUID() {
        return nlUUID;
    }

    public HashMap<QuickPlayerData, NameLayerRankEnum> getRanks() {
        return ranks;
    }

    public HashMap<NameLayerPermissions, Boolean> getPermissionsMap() {
        return permissionsMap;
    }

    public enum NameLayerPermissions{
        GUEST_INVITE_USER,
        MEMBER_INVITE_USER,
        MODERATOR_INVITE_USER,
        ADMIN_INVITE_USER,
        OWNER_INVITE_USER;
    }
}
