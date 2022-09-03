package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieAPI;
import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import sun.awt.AWTAccessor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CombatLogManager {

    private List<CombatSession> combatSessionList = new LinkedList<>();
    private List<UUID> playersKilledOffline = new LinkedList<>();
    public static final long TIME_TILL_OVER = 1000 * 20;

    public CombatLogManager(CivvieCorePlugin plugin) {

        File file = new File(plugin.getDataFolder(),"combatlogger.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(file);
        if(c.contains("list")){
            for(String s : c.getStringList("list")){
                playersKilledOffline.add(UUID.fromString(s));
            }
        }



        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BossBarManager.BossBarHolder bbh = CivvieAPI.getInstance().getBossBarManager().getBossbarsFor(player, "combatlog");
                    if (bbh != null) {
                        long closestTime = 0;
                        boolean removed = false;
                        for (CombatSession cs : getCombatSession(player)) {
                            if(((double) System.currentTimeMillis() > cs.getLastTimeCombat()+TIME_TILL_OVER)){
                                combatSessionList.remove(cs);
                                removed = true;
                                continue;
                            }
                            if (closestTime < cs.getLastTimeCombat())
                                closestTime = cs.getLastTimeCombat();
                        }
                        if(removed || (((double)System.currentTimeMillis()-closestTime)/(TIME_TILL_OVER))>1.0) {
                            if(getCombatSession(player).size()==0 || (((double)System.currentTimeMillis()-closestTime)/(TIME_TILL_OVER))>1.0){
                                bbh.setProgression(1);
                                bbh.getBossBar().setColor(BarColor.GREEN);
                                new BukkitRunnable(){
                                    @Override
                                    public void run() {
                                        CivvieAPI.getInstance().getBossBarManager().removeBossBar(bbh);
                                    }
                                }.runTaskLater(plugin,5);
                                return;
                            }
                        }
                        if(bbh.getBossBar().getColor()==BarColor.GREEN)
                            bbh.getBossBar().setColor(BarColor.YELLOW);
                        bbh.setProgression(1.0-(((double)System.currentTimeMillis()-closestTime)/(TIME_TILL_OVER)));
                    }
                }
            }
        }.runTaskTimer(plugin, 3, 3);
    }

    public List<CombatSession> getCombatSessionList() {
        return combatSessionList;
    }

    public List<UUID> getPlayersKilledOffline() {
        return playersKilledOffline;
    }

    public List<CombatSession> getCombatSession(Player player) {
        List<CombatSession> ses = new LinkedList<>();
        for (CombatSession combatSession : combatSessionList) {
            if (combatSession.isCombat(player))
                ses.add(combatSession);
        }
        return ses;
    }

    public CombatSession getCombatSession(Player player, Player player2) {
        for (CombatSession combatSession : combatSessionList) {
            if (combatSession.isCombat(player) && combatSession.isCombat(player2)) {
                return combatSession;
            }
        }
        return null;
    }

    public CombatSession createCombatSession(Player player1, Player player2) {
        CombatSession cs = new CombatSession(player1.getUniqueId(), player2.getUniqueId());
        this.combatSessionList.add(cs);

        BossBarManager.BossBarHolder bbh = CivvieAPI.getInstance().getBossBarManager().getBossbarsFor(player1, "combatlog");
        if (bbh == null) {
            bbh = CivvieAPI.getInstance().getBossBarManager().createBossBar("combatlog", player1, "Combat Timer", BarColor.YELLOW);
        }
        bbh.setProgression(1.0-(((double)System.currentTimeMillis()-cs.getLastTimeCombat())/(TIME_TILL_OVER)));

        BossBarManager.BossBarHolder bbh2 = CivvieAPI.getInstance().getBossBarManager().getBossbarsFor(player2, "combatlog");
        if (bbh2 == null) {
            bbh2 = CivvieAPI.getInstance().getBossBarManager().createBossBar("combatlog", player2, "Combat Timer", BarColor.YELLOW);
        }
        bbh2.setProgression(1.0-(((double)System.currentTimeMillis()-cs.getLastTimeCombat())/(TIME_TILL_OVER)));
        return cs;
    }

    public void removeSession(CombatSession combatSession) {
        this.combatSessionList.remove(combatSession);
    }

    public class CombatSession {

        private final UUID player1;
        private final UUID player2;
        private long lastTimeCombat;

        public CombatSession(UUID uuid, UUID uuid2) {
            this.player1 = uuid;
            this.player2 = uuid2;
            this.lastTimeCombat = System.currentTimeMillis();
        }

        public UUID getPlayer1() {
            return player1;
        }

        public UUID getPlayer2() {
            return player2;
        }

        public long getLastTimeCombat() {
            return lastTimeCombat;
        }

        public void setLastTimeCombat(long lastTimeCombat) {
            this.lastTimeCombat = lastTimeCombat;
        }

        public boolean isCombat(Player player) {
            if (player.getUniqueId().equals(player1) || player.getUniqueId().equals(player2))
                if(System.currentTimeMillis()-getLastTimeCombat()<=15*1000)
                    return true;
                return false;
        }
    }
}
