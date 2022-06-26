package me.zombie_striker.civviecore;

import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.managers.FactoryManager;
import me.zombie_striker.civviecore.util.ItemsUtil;
import me.zombie_striker.civviecore.util.OreDiscoverUtil;
import me.zombie_striker.ezinventory.EZGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

public class CivvieListener implements Listener {

    private final CivvieCorePlugin plugin;

    public CivvieListener(CivvieCorePlugin civvieCorePlugin) {
        this.plugin = civvieCorePlugin;
    }

    private final BlockFace[] faces_factory = new BlockFace[]{BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        CivWorld world = CivCore.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {
                CivBlock block = chunk.getBlockAt(event.getBlock().getLocation());
                if (block != null) {
                    if (block.getMaxReinforcement() > 0) {
                        if (block.getReinforcement() >= 0) {
                            event.setCancelled(true);
                            CivCore.getInstance().playReinforceProtection(event.getBlock().getLocation());
                            block.setReinforcement(block.getReinforcement() - 1);
                            chunk.removeCivBlock(block);
                        }
                    }
                } else {
                    if (event.getBlock().getType() == Material.STONE || event.getBlock().getType() == Material.DEEPSLATE) {
                        OreDiscoverUtil.populateOres(event.getBlock());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == Material.STICK) {
            if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
                CivWorld cw = CivCore.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
                CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
                for (FactoryBuild fb : cc.getFactories()) {
                    if (fb.getCraftingTable().equals(event.getClickedBlock().getLocation())) {
                        openFactoryGUI(event, fb);
                        return;
                    }
                }


                for (BlockFace blockFace : faces_factory) {
                    Block block = event.getClickedBlock().getRelative(blockFace);
                    if (block.getType() == Material.CHEST) {
                        Block furnace = event.getClickedBlock().getRelative(blockFace.getOppositeFace());
                        if (furnace.getType() == Material.FURNACE) {
                            Inventory chestinv = ((Container) block.getState()).getInventory();

                            for (FactoryManager.FactoryType factoryType : CivCore.getInstance().getFactoryManager().getTypes()) {
                                if (ItemsUtil.containsItems(factoryType.getIngredients(), chestinv)) {
                                    FactoryBuild fb = new FactoryBuild(event.getClickedBlock().getLocation(), block.getLocation(), furnace.getLocation(), factoryType);
                                    cc.addFactory(fb);
                                }
                            }
                        }
                    }
                }

            } else if (event.getClickedBlock().getType() == Material.FURNACE) {
                CivWorld cw = CivCore.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
                CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
                for (FactoryBuild fb : cc.getFactories()) {
                    if (fb.getFurnace().equals(event.getClickedBlock().getLocation())) {
                        Container container = (Container) event.getClickedBlock().getState();
                        if (container.getInventory().contains(Material.CHARCOAL)) {
                            if (fb.isRunning()) {
                                fb.setRunning(false);
                                fb.setRecipeTick(0);
                            } else {
                                event.getPlayer().sendMessage(Component.text("Running recipe: " + fb.getCurrentRecipe().getName()).color(TextColor.color(0, 200, 20)));
                                fb.setRunning(true);
                                fb.setRecipeTick(0);
                            }
                        } else {
                            event.getPlayer().sendMessage(Component.text("The factory does not have any fuel.").color(TextColor.color(200, 0, 0)));
                        }
                        return;
                    }
                }
            }
        }else if (event.getAction()==Action.RIGHT_CLICK_BLOCK && (event.getPlayer().getInventory().getItemInMainHand()==null||event.getPlayer().getInventory().getItemInMainHand().getType()!=Material.STICK)){
           //Auto Break crops on right click.
            if(event.getClickedBlock().getBlockData() instanceof Ageable && (((Ageable) event.getClickedBlock().getBlockData()).getAge()==((Ageable) event.getClickedBlock().getBlockData()).getMaximumAge())) {
                if (event.getClickedBlock().getType() == Material.WHEAT_SEEDS) {
                    event.getClickedBlock().breakNaturally();
                    event.getClickedBlock().setType(Material.WHEAT_SEEDS);
                } else if (event.getClickedBlock().getType() == Material.BEETROOTS) {
                    event.getClickedBlock().breakNaturally();
                    event.getClickedBlock().setType(Material.BEETROOTS);
                } else if (event.getClickedBlock().getType() == Material.POTATOES) {
                    event.getClickedBlock().breakNaturally();
                    event.getClickedBlock().setType(Material.POTATOES);
                } else if (event.getClickedBlock().getType() == Material.CARROTS) {
                    event.getClickedBlock().breakNaturally();
                    event.getClickedBlock().setType(Material.CARROTS);
                }
            }
        }else if (event.getAction()==Action.RIGHT_CLICK_BLOCK && (event.getPlayer().getInventory().getItemInMainHand()!=null&&event.getPlayer().getInventory().getItemInMainHand().getType()==Material.STICK)){

            CivWorld cw = CivCore.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
            CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
            for(CropBlock cb : cc.getCropBlocks()){
                if(cb.getLocation().equals(event.getClickedBlock().getLocation())){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("Time till fully grown: ").color(TextColor.color(20,200,20)).append(Component.text(formatTime(cb.getPlantTime()+cb.getGrowTime()-System.currentTimeMillis())).color(TextColor.color(150,150,150)));
                }
            }
        }
    }

    private void openFactoryGUI(PlayerInteractEvent event, FactoryBuild fb) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 27, fb.getType().getName()));
        int i = 0;
        for (FactoryRecipe fr : fb.getType().getRecipes()) {
            ezgui.addCallable(fr.getIcon(), (slot, isShiftClick, isRightClick) -> {
                fb.setCurrentRecipe(fr);
                fb.setRunning(true);
                event.getPlayer().sendMessage(Component.text("Setting recipe to: " + fr.getName()).color(TextColor.color(0, 200, 20)));
            }, i);
            i++;
        }
    }


    private ItemStack removeOneFromStack(ItemStack is) {
        is.setAmount(is.getAmount() - 1);
        return is;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getPlayer().getKiller() != null) {
            Player killer = event.getPlayer().getKiller();
            int pearlSlot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack is = killer.getInventory().getItem(i);
                if (is.getType() == Material.ENDER_PEARL && !ItemsUtil.isPrisonPearl(is)) {
                    pearlSlot = i;
                    break;
                }
            }
            if (pearlSlot == -1) {
                ItemStack is = killer.getInventory().getItemInMainHand();
                if (is.getType() == Material.ENDER_PEARL && !ItemsUtil.isPrisonPearl(is)) {
                    pearlSlot = 10;
                }
            }
            if (pearlSlot == -1) {
                ItemStack is = killer.getInventory().getItemInOffHand();
                if (is.getType() == Material.ENDER_PEARL && !ItemsUtil.isPrisonPearl(is)) {
                    pearlSlot = 11;
                }
            }
            if (pearlSlot > -1) {
                if (pearlSlot < 9) {
                    killer.getInventory().setItem(pearlSlot, removeOneFromStack(killer.getInventory().getItem(pearlSlot)));
                } else if (pearlSlot == 10) {
                    killer.getInventory().setItemInMainHand(removeOneFromStack(killer.getInventory().getItemInMainHand()));
                } else {
                    killer.getInventory().setItemInOffHand(removeOneFromStack(killer.getInventory().getItemInOffHand()));
                }

                String designation = CivCore.getInstance().getPearlManager().createPearl(event.getPlayer());

                if (killer.getInventory().firstEmpty() == -1) {
                    event.getPlayer().getWorld().dropItem(killer.getLocation(), ItemsUtil.createPrisonPearl(event.getPlayer(), killer, formatDate(System.currentTimeMillis()), 20, designation));
                } else {
                    killer.getInventory().addItem(ItemsUtil.createPrisonPearl(event.getPlayer(), killer, formatDate(System.currentTimeMillis()), 20, designation));
                }

            }
        }
    }

    public String formatTime(long time){
        if(time < 0)
            return "Now";
        StringBuilder sb = new StringBuilder();
        boolean addComma = false;
        if(time > 1000*60*60*24){
            int days = (int) (time/(1000*60*60*24));
            sb.append(days+" days");
            time -= days *(1000*60*60*24);
            addComma=true;
        }
        if(time > 1000*60*60){
            if(addComma)
                sb.append(", ");
            addComma=true;
            int days = (int) (time/(1000*60*60));
            sb.append(days+" hours");
            time -= days *(1000*60*60);
        }
        if(time > 1000*60){
            if(addComma)
                sb.append(", ");
            addComma=true;
            int days = (int) (time/(1000*60));
            sb.append(days+" minutes");
            time -= days *(1000*60);
        }
        return sb.toString();
    }

    public String formatDate(long time){
        Date date = new Date(time);
        String dateString = date.getDay()+"/"+date.getMonth()+"/"+date.getYear();
        return dateString;
    }


    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        CivCore.getInstance().getWorld(event.getWorld().getName()).getChunkAt(event.getChunk().getX(), event.getChunk().getZ()).unload();
    }

    @EventHandler
    public void onChunkGenerate(ChunkLoadEvent event) {
        if (event.isNewChunk()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = -63; y < 256; y++) {
                        Block block = event.getChunk().getBlock(x, y, z);
                        if (block.getType().name().contains("_ORE")) {
                            if (block.getType().name().contains("DEEPSLATE")) {
                                block.setType(Material.DEEPSLATE);
                            } else {
                                block.setType(Material.STONE);
                            }
                        }
                    }
                }
            }
        }
        CivChunk.load(event.getChunk().getX(), event.getChunk().getZ(), CivCore.getInstance().getWorld(event.getWorld().getName()));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        CivWorld world = CivCore.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {
                Material type = event.getBlockPlaced().getType();

                NameLayer nl = CivCore.getInstance().getReinforcingTo().get(event.getPlayer().getUniqueId());
                if (nl == null) {

                    if (event.getBlockPlaced().getType() == Material.STONE) {
                        CivBlock cb = chunk.getBlockAt(event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(null);
                        cb.setMaxReinforcement(-1);
                        cb.setReinforcement(-1);
                    }
                    if (event.getBlockPlaced().getType() == Material.DEEPSLATE) {
                        CivBlock cb = chunk.getBlockAt(event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(null);
                        cb.setMaxReinforcement(-1);
                        cb.setReinforcement(-1);
                    }


                    return;
                }
                Material reinfmat = CivCore.getInstance().getReinforceMaterial().get(event.getPlayer().getUniqueId());

                if (!event.getPlayer().getInventory().contains(reinfmat)) {
                    return;
                }


                switch (type) {
                    case BEETROOT_SEEDS:
                    case MELON_SEEDS:
                    case PUMPKIN_SEEDS:
                    case WHEAT_SEEDS:
                    case POTATOES:
                    case CARROTS:
                        CivBlock civBlock = chunk.getBlockAt(event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        if (civBlock == null) {
                            civBlock = new CivBlock(chunk, event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        }
                        CropBlock cp = new CropBlock(chunk, civBlock, event.getBlockPlaced().getLocation(), System.currentTimeMillis(), CivCore.getInstance().getGrowthManager().getGrowthFor(type, event.getBlockPlaced().getBiome()));
                        chunk.addCivBlock(cp);
                        chunk.getCropBlocks().add(cp);
                        cp.setOwner(nl);
                        cp.setMaxReinforcement(CivCore.getInstance().getReinforcelevel().get(event.getPlayer().getUniqueId()));
                        cp.setReinforcement(cp.getMaxReinforcement());
                        CivCore.getInstance().playReinforceProtection(cp.getLocation());
                        break;
                    default:
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(nl);
                        cb.setMaxReinforcement(CivCore.getInstance().getReinforcelevel().get(event.getPlayer().getUniqueId()));
                        cb.setReinforcement(cb.getMaxReinforcement());
                        CivCore.getInstance().playReinforceProtection(cb.getLocation());
                }
            }
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB)
            event.setCancelled(true);
    }
}
