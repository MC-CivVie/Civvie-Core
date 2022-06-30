package me.zombie_striker.civviecore.data;

public enum NameLayerRankEnum {

    OWNER(0),
    ADMIN(1),
    MODERATOR(2),
    MEMBER(3),
    GUEST(4);

    private final int rank;
    NameLayerRankEnum(int rank){
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
