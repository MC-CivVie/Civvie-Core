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
    private final List<BlockDropHolder> blockDropHolders = new LinkedList<>();

    private final ItemStack starter_book = new ItemStack(Material.WRITTEN_BOOK);

    public ItemManager(CivvieCorePlugin plugin) {

    }

    public List<ItemType> getItemTypes() {
        return itemTypes;
    }

    public ItemType getItemTypeByName(String name) {
        for (ItemType it : itemTypes) {
            if (it.getName().equalsIgnoreCase(name))
                return it;
        }
        return null;
    }

    public ItemType getItemTypeByMaterial(Material material) {
        return getItemTypeByMaterial(material, 0);
    }

    public ItemType getItemTypeByMaterial(Material material, int custommodeldata) {
        for (ItemType it : itemTypes) {
            if (it.getBaseMaterial() == material) {
                if (custommodeldata == 0 && !(it instanceof ItemCustomType))
                    return it;
                if (it instanceof ItemCustomType && ((ItemCustomType) it).getCustommodeldata() == custommodeldata)
                    return it;
            }
        }
        return null;
    }

    public List<BlockDropHolder> getBlockDropHolders() {
        return blockDropHolders;
    }

    public void init(CivvieCorePlugin plugin) {
        for (Material material : Material.values()) {
            ItemType type = new ItemType(material, material.name());
            itemTypes.add(type);
        }
        File folder1 = new File(plugin.getDataFolder(), "customitems");
        if (!folder1.exists())
            folder1.mkdirs();

        for (File file : folder1.listFiles()) {
            if (file.getName().endsWith("yml")) {
                try {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String name = config.getString("name");
                    String displayname = config.getString("displayname");
                    if (displayname == null) {
                        displayname = name;
                    }
                    int data = config.getInt("data");
                    Material material = Material.matchMaterial(config.getString("material"));
                    ItemCustomType customitem = new ItemCustomType(data, material, name, displayname);
                    itemTypes.add(customitem);
                } catch (Exception e43) {
                    e43.printStackTrace();
                }
            }
        }
        File folder = new File(plugin.getDataFolder(), "materials");
        if (!folder.exists())
            folder.mkdirs();

        for (File file : folder.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String name = config.getString("name");
                List<Material> material = new LinkedList<>();
                for (String s : config.getStringList("types")) {
                    material.add(Material.matchMaterial(s));
                }
                ItemSubType subtype = new ItemSubType(material, name);
                itemTypes.add(subtype);
            }
        }


        File folder2 = new File(plugin.getDataFolder(), "blockdrops");
        if (!folder2.exists())
            folder2.mkdirs();

        for (File file : folder2.listFiles()) {
            if (file.getName().endsWith("yml")) {
                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                String result = config.getString("dropresult");
                int amount = config.getInt("dropamount");
                Material material = Material.matchMaterial(config.getString("block"));
                BlockDropHolder blockDropHolder = new BlockDropHolder(material, result, amount);
                blockDropHolders.add(blockDropHolder);
            }
        }

        File starterbookfile = new File(plugin.getDataFolder(), "starterbook.yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(starterbookfile);


        BookMeta bm = (BookMeta) starter_book.getItemMeta();
        if (c.contains("pages")) {
            for (String page : c.getConfigurationSection("pages").getKeys(false)) {
                String text = c.getString("pages." + page + ".text");
                text = text.replaceAll("\\n", "\n");
                if (text != null)
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

    public class BlockDropHolder {

        private Material blockdrop;
        private String drop;
        private int dropAmount;

        public BlockDropHolder(Material block, String drop, int dropamount) {
            this.blockdrop = block;
            this.drop = drop;
            this.dropAmount = dropamount;
        }

        public int getDropAmount() {
            return dropAmount;
        }

        public Material getBlockdrop() {
            return blockdrop;
        }

        public String getDrop() {
            return drop;
        }
    }


    public class ItemType {
        private Material baseMaterial;
        private String name;

        public ItemType(Material baseMaterial, String name) {
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
            if(is==null)
                return false;
            if (is.getItemMeta().hasCustomModelData() && is.getItemMeta().getCustomModelData() != 0)
                return false;
            return baseMaterial == is.getType();
        }
    }

    public class ItemCustomType extends ItemType {

        private int custommodeldata;
        private String displayname;

        public ItemCustomType(int data, Material baseMaterial, String name, String displayname) {
            super(baseMaterial, name);
            this.custommodeldata = data;
            this.displayname = displayname;
        }

        public String getDisplayname() {
            return displayname;
        }

        public int getCustommodeldata() {
            return custommodeldata;
        }

        public boolean isType(ItemStack is) {
            if(is!=null)
            if (is.getItemMeta().hasCustomModelData() && is.getItemMeta().getCustomModelData() == getCustommodeldata())
                if (is.getType() == getBaseMaterial())
                    return true;
            return false;
        }
    }

    public class ItemSubType extends ItemType {
        private List<Material> types;

        public ItemSubType(List<Material> materials, String name) {
            super(Material.AIR, name);
            this.types = materials;
        }

        public List<Material> getTypes() {
            return types;
        }

        @Override
        public boolean isType(ItemStack is) {
            for (Material m : types) {
                if (m == is.getType())
                    return true;
            }
            return false;
        }
    }

    public static class ItemStorage {

        private ItemType itemType;
        private int amount;

        public ItemStorage(ItemType itemType, int amount) {
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
