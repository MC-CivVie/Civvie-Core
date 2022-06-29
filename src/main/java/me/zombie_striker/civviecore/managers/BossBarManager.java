package me.zombie_striker.civviecore.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BossBarManager {

    private List<BossBarHolder> bossBarHolderList = new LinkedList<>();

    public BossBarManager(){

    }

    public BossBarHolder createBossBar(String internalname, Player player, String name, BarColor barColor){
        BossBarHolder bbh =new BossBarHolder(internalname, player.getUniqueId(),name,BarStyle.SOLID, barColor);
        bossBarHolderList.add(bbh);
        bbh.getBossBar().addPlayer(player);
        return bbh;
    }
    public void removeBossBar(BossBarHolder bossBarHolder){
        bossBarHolderList.remove(bossBarHolder);
        bossBarHolder.getBossBar().removePlayer(Bukkit.getPlayer(bossBarHolder.getUuid()));
    }


    public BossBarHolder getBossbarsFor(Player player, String internalName){
        for(BossBarHolder b : bossBarHolderList){
            if(b.getUuid().equals(player.getUniqueId()))
                if(b.getInternalName().equals(internalName))
                return b;
        }
        return null;
    }
    public List<BossBarHolder> getBossbarsFor(Player player){
        List<BossBarHolder> bbh = new LinkedList<>();
        for(BossBarHolder b : bossBarHolderList){
            if(b.getUuid().equals(player.getUniqueId()))
                bbh.add(b);
        }
        return bbh;
    }


    public List<BossBarHolder> getBossBarHolderList() {
        return bossBarHolderList;
    }

    public class BossBarHolder{
        private final UUID uuid;
        private final String internalName;
        private final @NotNull BossBar bossBar;

        public BossBarHolder(String internalname, UUID uuid, String name, BarStyle style, BarColor color){
            this.bossBar = Bukkit.createBossBar(name,color,style);
            bossBar.setVisible(true);
            this.uuid = uuid;
            this.internalName = internalname;
        }

        public void setTitle(String title){
            this.bossBar.setTitle(title);
        }
        public void setProgression(double progression){
            this.bossBar.setProgress(progression);
        }
        public double getProgression(){
            return bossBar.getProgress();
        }
        public String getTitle(){
            return bossBar.getTitle();
        }

        public String getInternalName() {
            return internalName;
        }

        public UUID getUuid() {
            return uuid;
        }

        public BossBar getBossBar() {
            return bossBar;
        }
    }
}
