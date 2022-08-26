package me.zombie_striker.civviecore.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class Snitch {

    private Location location;
    private int radius;
    private JukeType type;
    private String name;
    private final List<JukeRecord> recordList = new LinkedList<>();

    public Snitch(Location location, int radius, JukeType type){
        this.radius = radius;
        this.location = location;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JukeType getType() {
        return type;
    }

    public int getRadius() {
        return radius;
    }

    public Location getLocation() {
        return location;
    }

    public void addJukeRecord(JukeRecord jr ){
        this.recordList.add(jr);
    }

    public List<JukeRecord> getRecordList() {
        return recordList;
    }
    public void removeJukeRecord(JukeRecord jr){
        this.recordList.remove(jr);
    }

    public enum JukeType{
        JUKEBOX,
        NOTEBLOCK,
        ;
    }
    public static abstract class JukeRecord{

        private long time;
        private Snitch jukeBlock;

        public JukeRecord(long time, Snitch jukeBlock){
            this.time = time;
            this.jukeBlock = jukeBlock;
        }

        public Snitch getJukeBlock() {
            return jukeBlock;
        }

        public long getTime() {
            return time;
        }
        public abstract void onCall(CivBlock civBlock);
    }
    public static class PlayerEnterJukeRecord extends JukeRecord{
        private QuickPlayerData quickPlayerData;
        private Location enter;

        public PlayerEnterJukeRecord(long time, Snitch jukeBlock, QuickPlayerData quickPlayerData) {
            super(time, jukeBlock);
            this.quickPlayerData = quickPlayerData;
        }

        public QuickPlayerData getQuickPlayerData() {
            return quickPlayerData;
        }

        public Location getEnter() {
            return enter;
        }

        @Override
        public void onCall(CivBlock civBlock) {
            for(QuickPlayerData qpd : civBlock.getOwner().getRanks().keySet()){
                Player online = Bukkit.getPlayer(qpd.getUuid());
                if(online !=null){
                    online.sendMessage(Component.text("[JA] "+quickPlayerData.getLastName()+" entered "+(getJukeBlock().getName()!=null?"\""+getJukeBlock().getName()+"\"": "Snitch at "+getJukeBlock().getLocation().getBlockX()+", "+getJukeBlock().getLocation().getBlockY()+", "+getJukeBlock().getLocation().getBlockZ())+".").color(TextColor.color(10,200,100)));
                }
            };
        }
    }
}
