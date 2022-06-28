package me.zombie_striker.civviecore.managers;

import me.zombie_striker.civviecore.CivvieCorePlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ItemManager {

    private final List<ItemType> itemTypes = new LinkedList<>();

    private final ItemStack starter_book = new ItemStack(Material.WRITTEN_BOOK);

    public ItemManager(CivvieCorePlugin plugin){

    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public ItemType getItemTypeByName(String name){
        for(ItemType it: itemTypes){
            if(it.getName().equalsIgnoreCase(name))
                return it;
        }
        return null;
    }

    public ItemType getItemTypeByMaterial(Material material) {
        for(ItemType it: itemTypes){
            if(it.getBaseMaterial() == material)
                return it;
        }
        return null;
    }

    public void init(CivvieCorePlugin plugin) {
        for(Material material : Material.values()){
            ItemType type = new ItemType(material,material.name());
            itemTypes.add(type);
        }
        File folder = new File(plugin.getDataFolder(),"materials");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                List<Material> material = new LinkedList<>();
                for(String s : config.getStringList("types")){
                    material.add(Material.matchMaterial(s));
                }
                ItemSubType subtype = new ItemSubType(material, name);
                itemTypes.add(subtype);
            }
        }

        File starterbookfile = new File(plugin.getDataFolder(),"starterbook.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(starterbookfile);


        BookMeta bm = (BookMeta) starter_book.getItemMeta();
        if(c.contains("pages")) {
            for (String page : c.getConfigurationSection("pages").getKeys(false)){
                String text = c.getString("pages."+page+".text");
                text = text.replaceAll("\\n","\n");
                if(text!=null)
                bm.addPages(Component.text(text));
            }
        }
        bm.setAuthor(c.getString("author"));
        bm.setTitle(c.getString("title"));
        starter_book.setItemMeta(bm);
    }

    public ItemStack getStarterBook() {
        return starter_book;
    }

    public class ItemType{
        private Material baseMaterial;
        private String name;

        public ItemType(Material baseMaterial, String name){
            this.baseMaterial = baseMaterial;
            this.name = name;
        }

        public Material getBaseMaterial() {
            return baseMaterial;
        }

        public String getName() {
            return name;
        }

        public boolean isType(ItemStack is) {
            return baseMaterial==is.getType();
        }
    }
    public class ItemSubType extends  ItemType{
        private List<Material> types;

        public ItemSubType(List<Material> materials, String name){
            super(Material.AIR,name);
            this.types = materials;
        }

        public List<Material> getTypes() {
            return types;
        }

        @Override
        public boolean isType(ItemStack is) {
            for(Material m : types){
                if(m==is.getType())
                    return true;
            }
            return false;
        }
    }

    public static class ItemStorage{

        private ItemType itemType;
        private int amount;

        public ItemStorage(ItemType itemType, int amount){
            this.amount = amount;
            this.itemType = itemType;
        }

        public ItemType getItemType() {
            return itemType;
        }

        public int getAmount() {
            return amount;
        }
    }

}
