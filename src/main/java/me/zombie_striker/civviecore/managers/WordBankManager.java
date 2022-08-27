package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class WordBankManager {

    private List<String> nouns;
    private List<String> formats;


    public WordBankManager() {

    }

    public void init(CivvieCorePlugin plugin) {
        File wordbank = new File(plugin.getDataFolder(), "wordbank.yml");
        FileConfiguration fc = YamlConfiguration.loadConfiguration(wordbank);
        nouns = fc.getStringList("nouns");
        formats = fc.getStringList("formats");
    }

    public String getNoun(long longs) {
        return nouns.get(new Random(longs).nextInt(nouns.size()));
    }

    public String getFormat(long longs) {
        return formats.get(new Random(longs).nextInt(formats.size()));
    }

    public String get(String text) {
        String texty = text;
        for (int c = 0; c < texty.length(); c++) {
            char cc = texty.charAt(c);
            if (!Character.isDigit(cc)) {
                texty = texty.replaceAll(cc + "", Math.abs((byte) cc) + "");
            }
        }
        String first = texty.substring(0, texty.length() / 3);
        String second = texty.substring(texty.length() / 3);
        long l = Long.parseLong(first);
        long l2 = Long.parseLong(second);

        String format = getFormat(l);
        do {
            if (format.contains("%n")) {
                format = format.replaceFirst("%n", getNoun(l2));
                long l3 = l2;
                l2 &= l;
                l2+=l3;
            }
        }while (format.contains("%"));
        return format;
    }
}
