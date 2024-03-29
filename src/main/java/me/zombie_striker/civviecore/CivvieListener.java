package me.zombie_striker.civviecore;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.zombie_striker.civviecore.data.*;
import me.zombie_striker.civviecore.enchantments.GenericEnchant;
import me.zombie_striker.civviecore.managers.*;
import me.zombie_striker.civviecore.util.ItemsUtil;
import me.zombie_striker.ezinventory.EZGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.TripwireHook;
import org.bukkit.boss.BarColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static org.bukkit.Material.*;

public class CivvieListener implements Listener {

    private final CivvieCorePlugin plugin;

    public CivvieListener(CivvieCorePlugin civvieCorePlugin) {
        this.plugin = civvieCorePlugin;
    }

    private final BlockFace[] faces_factory = new BlockFace[]{BlockFace.UP, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH};

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        boolean customItem = false;
        for (ItemStack is : event.getInventory().getMatrix()) {
            if (is != null && is.hasItemMeta() && is.getItemMeta().hasCustomModelData()) {
                customItem = true;
                break;
            }
        }
        if (customItem) {
            for (CraftingManager.RecipeRestore restored : CivvieAPI.getInstance().getCraftingManager().getRestoredRecipes()) {
                if (restored.isRecipe(event.getInventory().getMatrix())) {
                    event.getInventory().setResult(ItemsUtil.createItem(restored.getItemType()));
                    return;
                }
            }
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setExpToDrop(0);

        for (ItemManager.BlockDropHolder blockDropHolder : CivvieAPI.getInstance().getItemManager().getBlockDropHolders()) {
            if (blockDropHolder.getBlockdrop() == event.getBlock().getType()) {
                event.setDropItems(false);
                ItemManager.ItemType type = CivvieAPI.getInstance().getItemManager().getItemTypeByName(blockDropHolder.getDrop());
                if (type != null) {
                    if (type instanceof ItemManager.ItemCustomType) {
                        ItemStack ts = new ItemStack(type.getBaseMaterial(), blockDropHolder.getDropAmount());
                        ItemMeta im = ts.getItemMeta();
                        im.displayName(Component.text(((ItemManager.ItemCustomType) type).getDisplayname()));
                        im.setCustomModelData(((ItemManager.ItemCustomType) type).getCustommodeldata());
                        ts.setItemMeta(im);

                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ts);
                    } else {
                        ItemStack ts = new ItemStack(type.getBaseMaterial(), blockDropHolder.getDropAmount());
                        event.getBlock().getWorld().dropItem(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ts);
                    }
                }
                break;
            }
        }

        CivWorld world = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {
            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {

                for (FactoryBuild fb : new LinkedList<>(chunk.getFactories())) {
                    if (fb.getFurnace().equals(event.getBlock().getLocation())) {
                        if (fb.isRunning()) {
                            fb.setRunning(false);
                            fb.setRecipeTick(0);
                        }
                    }
                    if (fb.getCraftingTable().equals(event.getBlock().getLocation())) {
                        if (fb.isRunning()) {
                            fb.setRunning(false);
                            fb.setRecipeTick(0);
                        }
                        chunk.removeFactory(fb);
                        for (ItemManager.ItemStorage ing : fb.getType().getIngredients()) {
                            if (!(ing.getItemType() instanceof ItemManager.ItemSubType)) {
                                ItemStack is = new ItemStack(ing.getItemType().getBaseMaterial(), Math.max(1, ing.getAmount() / 4));
                                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), is);
                            }
                        }
                    }
                }

                CropBlock cblock = null;
                if ((cblock = chunk.getCropAt(event.getBlock().getLocation())) != null) {
                    chunk.removeCivBlock(cblock);
                    chunk.removeCropBlock(cblock);
                }


                CivBlock block = chunk.getBlockAt(event.getBlock().getLocation());

                if (block == null && event.getBlock().getBlockData() instanceof Bisected) {
                    if (((Bisected) event.getBlock().getBlockData()).getHalf() == Bisected.Half.TOP) {
                        block = chunk.getBlockAt(event.getBlock().getLocation().subtract(0, 1, 0));
                    } else {
                        block = chunk.getBlockAt(event.getBlock().getLocation().add(0, 1, 0));
                    }
                }


                if (block != null) {
                    if (block.getMaxReinforcement() > 0) {
                        if (block.getReinforcement() > 0) {
                            if (block.getOwner() != null)
                                for (Map.Entry<QuickPlayerData, NameLayerRankEnum> e : block.getOwner().getRanks().entrySet()) {
                                    if (e.getKey().getUuid().equals(event.getPlayer().getUniqueId())) {
                                        //TODO: Check if group can break blocks.
                                        chunk.removeCivBlock(block);
                                        if (block.getReinforcedWith() != null)
                                            event.getPlayer().getInventory().addItem(new ItemStack(block.getReinforcedWith()));
                                        return;
                                    }
                                }

                            event.setCancelled(true);
                            CivvieAPI.getInstance().playReinforceProtection(event.getBlock().getLocation());
                            block.setReinforcement(block.getReinforcement() - 1);

                            BossBarManager.BossBarHolder bbh = CivvieAPI.getInstance().getBossBarManager().getBossbarsFor(event.getPlayer(), "reinforcebreak");
                            if (bbh == null) {
                                bbh = CivvieAPI.getInstance().getBossBarManager().createBossBar("reinforcebreak", event.getPlayer(), "Reinforcement: 0/0", BarColor.WHITE);
                            }
                            bbh.setTitle("Reinforce:  " + block.getReinforcement() + "/" + block.getMaxReinforcement());
                            bbh.setProgression(((double) block.getReinforcement()) / block.getMaxReinforcement());
                        } else {
                            chunk.removeCivBlock(block);
                            BossBarManager.BossBarHolder bbh = CivvieAPI.getInstance().getBossBarManager().getBossbarsFor(event.getPlayer(), "reinforcebreak");
                            if (bbh != null)
                                CivvieAPI.getInstance().getBossBarManager().removeBossBar(bbh);
                        }
                    }
                } else {
                    if (event.getBlock().getType() == Material.STONE || event.getBlock().getType() == Material.DEEPSLATE || event.getBlock().getType()==NETHERRACK) {
                        CivvieAPI.getInstance().getOreDiscoverManager().populateOres(event.getBlock());
                    }
                }

            }
        }
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        event.setExperience(0);
        List<Entity> nearby = event.getEntity().getNearbyEntities(25, 25, 25);
        int nearbyAlive = 0;
        for (Entity e : nearby) {
            if (e.getType().isAlive()) {
                nearbyAlive++;
            }
        }
        if (nearbyAlive > 25) {
            event.setCancelled(true);

        }
    }

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        CivWorld cw = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        CivChunk cc = cw.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        if (cc != null && cc.getBlockAt(event.getBlock().getLocation()) != null) {
            if (event.getChangedBlockData().getMaterial() == AIR)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSmelt(FurnaceSmeltEvent event) {
        if (event.getSource().hasItemMeta() && event.getSource().getItemMeta().hasCustomModelData())
            event.setCancelled(true);
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        NameLayer same = null;
        CivWorld cw = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        CivChunk cc = cw.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        if (cc != null && cc.getBlockAt(event.getBlock().getLocation()) != null) {
            same = cc.getBlockAt(event.getBlock().getLocation()).getOwner();
        }
        for (Block pistonMove : event.getBlocks()) {
            CivChunk cc2 = cw.getChunkAt(pistonMove.getChunk().getX(), pistonMove.getChunk().getZ());
            if (cc2 != null) {
                CivBlock cb2 = cc2.getBlockAt(pistonMove.getLocation());
                if (cb2 != null && cb2.getOwner() != same) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        NameLayer same = null;
        CivWorld cw = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        CivChunk cc = cw.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        if (cc != null && cc.getBlockAt(event.getBlock().getLocation()) != null) {
            same = cc.getBlockAt(event.getBlock().getLocation()).getOwner();
        }
        for (Block pistonMove : event.getBlocks()) {
            CivChunk cc2 = cw.getChunkAt(pistonMove.getChunk().getX(), pistonMove.getChunk().getZ());
            if (cc2 != null) {
                CivBlock cb2 = cc2.getBlockAt(pistonMove.getLocation());
                if (cb2 != null && cb2.getOwner() != same) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPing(PaperServerListPingEvent event) {
        List<UUID> uuids = CivvieAPI.getInstance().getIpToPlayerManager().getUUIDsFor(event.getAddress().getHostAddress());
        if (uuids != null) {
            PearlManager.PearlData pearlData = null;
            String name = null;
            String fuel = "";
            for (UUID uuid : uuids) {
                if (CivvieAPI.getInstance().getPearlManager().isPearled(Bukkit.getOfflinePlayer(uuid))) {
                    name = Bukkit.getOfflinePlayer(uuid).getName();
                    pearlData = CivvieAPI.getInstance().getPearlManager().getPearlData(uuid);
                    if (pearlData.updateFuel()) {
                        CivvieAPI.getInstance().getPearlManager().freePearl(pearlData);
                    }
                    fuel = String.format("%,.2f", pearlData.getFuel());
                }
            }
            if (pearlData != null) {
                event.motd(Component.text("--==xx(Civvie)xx==--\n").color(TextColor.color(200, 10, 50)).append(Component.text("Pearl for " + name + " is located at " + pearlData.getLocation().getX() + ", " + pearlData.getLocation().getY() + ", " + pearlData.getLocation().getZ() + " with health " + fuel + ".").color(TextColor.color(122, 21, 53))));
                return;
            }
        }
        event.motd(Component.text("--==xx(Civvie)xx==--\n").color(TextColor.color(200, 10, 50)).append(Component.text("Fight, Negotiate, and Win!").color(TextColor.color(50, 60, 50))));
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        event.setExpToDrop(0);
    }

    @EventHandler
    public void onKillEntity(EntityDeathEvent event) {
        event.setDroppedExp(0);
        if (event.getEntityType() == EntityType.SQUID || event.getEntityType() == EntityType.GLOW_SQUID) {
            if (new Random().nextInt(5) == 0) {
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(ENDER_PEARL));
            }
        }
        if (event.getEntityType() == EntityType.COW ||
                event.getEntityType() == EntityType.CHICKEN ||
                event.getEntityType() == EntityType.PIG ||
                event.getEntityType() == EntityType.HORSE ||
                event.getEntityType() == EntityType.MULE || event.getEntityType() == EntityType.DONKEY ||
                event.getEntityType() == EntityType.AXOLOTL ||
                event.getEntityType() == EntityType.BAT ||
                event.getEntityType() == EntityType.WOLF ||
                event.getEntityType() == EntityType.OCELOT ||
                event.getEntityType() == EntityType.CAT ||
                event.getEntityType() == EntityType.TURTLE
        ) {
            event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), new ItemStack(BONE));
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND)
            return;
        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            if (CivvieAPI.getInstance().getPlayerStateManager().getPlayerStatesOf(event.getPlayer().getUniqueId(), PlayerStateManager.InspectReinforecePlayerState.class).size() > 0) {

                CivWorld cw = CivvieAPI.getInstance().getWorld(event.getPlayer().getWorld().getName());
                CivChunk chunk = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
                if (chunk != null) {
                    CivBlock cb = chunk.getBlockAt(event.getClickedBlock().getLocation());
                    if (cb != null) {
                        event.setCancelled(true);
                        if (cb.getOwner() != null)
                            if (cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                event.getPlayer().sendMessage(Component.text("Owner: " + cb.getOwner().getName() + "   :   " + cb.getReinforcement() + " / " + cb.getMaxReinforcement()).color(TextColor.color(50, 200, 50)));
                            } else {
                                event.getPlayer().sendMessage(Component.text("Owner: ????   :   " + cb.getReinforcement() + " / " + cb.getMaxReinforcement()).color(TextColor.color(200, 50, 50)));
                            }
                        return;
                    }
                }
            }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == GRINDSTONE) {
            event.setCancelled(true);
        }

        if (event.getAction().isRightClick() && event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == ENDER_PEARL) {
            event.setCancelled(true);
            if (ItemsUtil.isPrisonPearl(event.getPlayer().getInventory().getItemInMainHand())) {

                int slotOFEssence = -1;
                for (int i = 0; i < event.getPlayer().getInventory().getSize(); i++) {
                    ItemStack is = event.getPlayer().getInventory().getItem(i);
                    if (is != null && is.getType() == ENDER_EYE) {
                        slotOFEssence = i;
                        break;
                    }
                }
                if (slotOFEssence == -1)
                    return;

                PearlManager.PearlData pd = ItemsUtil.getPearledPlayerFromPearl(event.getPlayer().getInventory().getItemInMainHand());
                pd.updateFuel();
                if (pd.getFuel() < 90) {
                    pd.setFuel(pd.getLastRefuel() + 10);
                    pd.updateFuel();
                    event.getPlayer().getInventory().setItemInMainHand(ItemsUtil.createPrisonPearl(Bukkit.getOfflinePlayer(pd.getUuid()), Bukkit.getOfflinePlayer(pd.getKiller()), ItemsUtil.formatDate(pd.getTimeKilled()), ItemsUtil.formatTime(pd.getLastRefuel()), (int) (pd.getFuel()), pd.getDesignation()));
                    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 1, 2);

                    ItemStack is = event.getPlayer().getInventory().getItem(slotOFEssence);
                    if (is.getAmount() == 1) {
                        is = null;
                    } else {
                        is.setAmount(is.getAmount() - 1);
                    }
                    event.getPlayer().getInventory().setItem(slotOFEssence, is);

                } else {
                    event.getPlayer().sendMessage(Component.text("Pearl is at full health.").color(TextColor.color(100, 200, 100)));
                }
            } else {
                event.getPlayer().sendMessage(Component.text("This is a prison pearl. Kill a player while this is in your hotbar to imprison them.").color(TextColor.color(100, 200, 100)));

            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == ENCHANTING_TABLE) {
            event.setCancelled(true);
            openEnchantGUI(event.getPlayer());
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == TRIPWIRE_HOOK) {
            TripwireHook tripwireHook = (TripwireHook) event.getClickedBlock().getBlockData();
            if (event.getClickedBlock().getRelative(tripwireHook.getFacing().getOppositeFace()).getType() == BARREL) {
                if (event.getPlayer().getInventory().getItemInMainHand().getType() == GLASS_BOTTLE) {
                    BreweryManager.Brew brew = CivvieAPI.getInstance().getBreweryManager().getBrew(event.getClickedBlock().getRelative(tripwireHook.getFacing().getOppositeFace()));
                    if(brew==null){
                        return;
                    }

                    ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                    if (hand.getAmount() == 1) {
                        hand = null;
                    } else {
                        if (event.getPlayer().getInventory().firstEmpty() != -1) {
                            hand.setAmount(hand.getAmount() - 1);
                        } else {
                            return;
                        }
                    }
                    event.getPlayer().getInventory().setItemInMainHand(hand);

                    ItemStack potion = new ItemStack(POTION);
                    PotionMeta pm = (PotionMeta) potion.getItemMeta();
                    pm.setColor(brew.getColor());
                    pm.displayName(Component.text(brew.getDisplayName()));
                    potion.setItemMeta(pm);
                    event.getPlayer().getInventory().addItem(potion);
                }
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == WATER_CAULDRON) {
            Block down = event.getClickedBlock().getRelative(BlockFace.DOWN);
            if (down.getType() == MAGMA_BLOCK || down.getType() == FIRE) {

                if (event.getPlayer().getInventory().getItemInMainHand() != null) {

                    if (event.getPlayer().getInventory().getItemInMainHand().getType() == BUCKET) {
                        if (CivvieAPI.getInstance().getBreweryManager().getWorts().containsKey(event.getClickedBlock().getLocation())) {
                            List<ItemManager.ItemType> types = CivvieAPI.getInstance().getBreweryManager().getWorts().get(event.getClickedBlock().getLocation());
                            long time = CivvieAPI.getInstance().getBreweryManager().getBrewtimes().get(event.getClickedBlock().getLocation());

                            int timescore = (int) ((System.currentTimeMillis() - time) / (1000 * 60));
                            new BukkitRunnable() {
                                public void run() {
                                    if (event.getPlayer().getInventory().getItemInMainHand().getType() == WATER_BUCKET) {
                                        ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                                        List<Component> lore;
                                        if (hand.hasItemMeta() && hand.getItemMeta().hasLore()) {
                                            lore = hand.getItemMeta().lore();
                                        } else {
                                            lore = new LinkedList<>();
                                        }
                                        lore.add(Component.text("Percent Full: 100").color(TextColor.color(200, 200, 200)));
                                        lore.add(Component.text("Brewing Time: " + timescore).color(TextColor.color(200, 200, 200)));
                                        lore.add(Component.text("Ingredients:").color(TextColor.color(200, 200, 200)));
                                        for (ItemManager.ItemType type : types) {
                                            lore.add(Component.text("-" + type.getName()).color(TextColor.color(200, 200, 200)));
                                        }
                                        CivvieAPI.getInstance().getBreweryManager().getWorts().remove(event.getClickedBlock().getLocation());
                                    }
                                }
                            }.runTaskLater(CivvieAPI.getInstance().getPlugin(), 0);
                        }
                    }


                    ItemManager.ItemType type = CivvieAPI.getInstance().getItemManager().getItemTypeByMaterial(event.getPlayer().getInventory().getItemInMainHand());
                    if (CivvieAPI.getInstance().getBreweryManager().getValidIngredients().contains(type)) {
                        if (CivvieAPI.getInstance().getBreweryManager().getWorts().containsKey(event.getClickedBlock().getLocation())&&CivvieAPI.getInstance().getBreweryManager().getWorts().get(event.getClickedBlock().getLocation()).size() >= 10) {
                            event.getPlayer().sendMessage("The cauldron is overflowing with items!");
                        } else {
                            CivvieAPI.getInstance().getBreweryManager().addIngredientToWort(event.getClickedBlock().getLocation(), type);
                            ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
                            if (hand.getAmount() == 1) {
                                hand = null;
                            } else {
                                hand.setAmount(hand.getAmount() - 1);
                            }
                            event.getPlayer().getInventory().setItemInMainHand(hand);
                            event.getPlayer().getWorld().playSound(event.getClickedBlock().getLocation(), Sound.ENTITY_PLAYER_SPLASH, 1f, 1.1f);
                        }
                    }else{
                        event.getPlayer().sendMessage("You can't put this in a brew!");
                    }
                }
            }
        }


        if (event.getAction().isRightClick() && event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType() == EMERALD) {
            ItemStack is = event.getPlayer().getInventory().getItemInMainHand();
            if (is.getAmount() > 1) {
                is.setAmount(is.getAmount() - 1);
            } else {
                is = null;
            }
            event.getPlayer().getInventory().setItemInMainHand(is);
            event.getPlayer().setLevel(event.getPlayer().getLevel() + 1);
            event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0.6f);
            event.setCancelled(true);
        }

        if (event.getAction().isRightClick() && event.getItem() != null && event.getItem().getType() == ENDER_EYE) {
            event.setCancelled(true);
        }


        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (!event.isBlockInHand() || !event.getPlayer().isSneaking()) && event.getClickedBlock().getType() == ENDER_CHEST) {
            event.setCancelled(true);
        }
        if (event.getAction().isRightClick() && ItemsUtil.isPrisonPearl(event.getPlayer().getInventory().getItemInMainHand())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getPlayer().getInventory().getItemInMainHand());
            if (pearlData == null) {
                event.getPlayer().getInventory().setItemInMainHand(null);
                return;
            }
            if (pearlData.updateFuel()) {
                event.getPlayer().getInventory().setItemInMainHand(null);
            } else {
                event.getPlayer().getInventory().setItemInMainHand(ItemsUtil.createPrisonPearl(Bukkit.getOfflinePlayer(pearlData.getUuid()), Bukkit.getOfflinePlayer(pearlData.getKiller()), ItemsUtil.formatDate(pearlData.getTimeKilled()), ItemsUtil.formatTime(pearlData.getLastRefuel()), (int) (pearlData.getFuel()), pearlData.getDesignation()));
            }
            event.setCancelled(true);
        }

        if (event.getAction().isRightClick() && ItemsUtil.isPrisonPearl(event.getPlayer().getInventory().getItemInOffHand())) {
            event.setCancelled(true);
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (!event.isBlockInHand() || !event.getPlayer().isSneaking())) {
            CivWorld cv = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
            CivChunk cc = cv.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
            if (cc != null) {
                CivBlock cb = null;
                switch (event.getClickedBlock().getType()) {
                    case FURNACE:
                    case BARREL:
                    case BLAST_FURNACE:
                    case ACACIA_FENCE_GATE:
                    case BIRCH_FENCE_GATE:
                    case CRIMSON_FENCE_GATE:
                    case DARK_OAK_FENCE_GATE:
                    case JUNGLE_FENCE_GATE:
                    case MANGROVE_FENCE_GATE:
                    case OAK_FENCE_GATE:
                    case SPRUCE_FENCE_GATE:
                    case WARPED_FENCE_GATE:
                    case ACACIA_TRAPDOOR:
                    case BIRCH_TRAPDOOR:
                    case CRIMSON_TRAPDOOR:
                    case DARK_OAK_TRAPDOOR:
                    case IRON_TRAPDOOR:
                    case JUNGLE_TRAPDOOR:
                    case MANGROVE_TRAPDOOR:
                    case OAK_TRAPDOOR:
                    case SPRUCE_TRAPDOOR:
                    case WARPED_TRAPDOOR:
                        if ((cb = cc.getBlockAt(event.getClickedBlock().getLocation())) != null &&
                                cb.getOwner() != null && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(Component.text("This block is locked.").color(TextColor.color(200, 10, 10)));
                            return;
                        }
                        break;
                    case ACACIA_DOOR:
                    case BIRCH_DOOR:
                    case DARK_OAK_DOOR:
                    case JUNGLE_DOOR:
                    case OAK_DOOR:
                    case SPRUCE_DOOR:
                    case MANGROVE_DOOR:
                    case IRON_DOOR:
                        if ((cb = cc.getBlockAt(event.getClickedBlock().getLocation())) != null && cb.getOwner() != null && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(Component.text("This door is locked.").color(TextColor.color(200, 10, 10)));
                            return;
                        }
                        if (event.getClickedBlock().getBlockData() instanceof Bisected) {
                            Bisected bs = (Bisected) event.getClickedBlock().getBlockData();
                            if (bs.getHalf() == Bisected.Half.TOP) {
                                if ((cb = cc.getBlockAt(event.getClickedBlock().getLocation().subtract(0, 1, 0))) != null && cb.getOwner() != null && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                    event.setCancelled(true);
                                    event.getPlayer().sendMessage(Component.text("This door is locked.").color(TextColor.color(200, 10, 10)));
                                    return;
                                }
                            } else {
                                if ((cb = cc.getBlockAt(event.getClickedBlock().getLocation().add(0, 1, 0))) != null && cb.getOwner() != null && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                    event.setCancelled(true);
                                    event.getPlayer().sendMessage(Component.text("This door is locked.").color(TextColor.color(200, 10, 10)));
                                    return;
                                }
                            }
                        }
                        break;
                    case CHEST:
                    case TRAPPED_CHEST:
                        if ((cb = cc.getBlockAt(event.getClickedBlock().getLocation())) != null && cb.getOwner() != null && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(Component.text("This door is locked.").color(TextColor.color(200, 10, 10)));
                            return;
                        }
                        if (event.getClickedBlock().getBlockData() instanceof DoubleChest) {
                            DoubleChest doubleChest = (DoubleChest) event.getClickedBlock().getBlockData();
                            if (((cb = cc.getBlockAt(((DoubleChest) doubleChest.getLeftSide()).getLocation())) != null || (cb = cc.getBlockAt(((DoubleChest) doubleChest.getRightSide()).getLocation())) != null) && !cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                event.setCancelled(true);
                                event.getPlayer().sendMessage(Component.text("This door is locked.").color(TextColor.color(200, 10, 10)));
                                return;
                            }
                        }
                    default:
                        break;
                }
            }
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && (event.getPlayer().getInventory().getItemInMainHand() == null || event.getPlayer().getInventory().getItemInMainHand().getType() == AIR)) {
            if (event.getClickedBlock().getType() == Material.CRAFTING_TABLE) {
                CivWorld cw = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
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

                            FactoryManager.FactoryType factoryTypefinal = null;

                            for (FactoryManager.FactoryType factoryType : CivvieAPI.getInstance().getFactoryManager().getTypes()) {
                                if (ItemsUtil.containsItems(factoryType.getIngredients(), chestinv)) {
                                    if (factoryTypefinal == null || factoryTypefinal.getIngredients().size() < factoryType.getIngredients().size())
                                        factoryTypefinal = factoryType;
                                }
                            }
                            if (factoryTypefinal != null) {
                                ItemsUtil.removeItemStorage(factoryTypefinal.getIngredients(), chestinv);
                                FactoryBuild fb = new FactoryBuild(event.getClickedBlock().getLocation(), furnace.getLocation(), block.getLocation(), factoryTypefinal);
                                cc.addFactory(fb);
                                event.getPlayer().sendMessage(Component.text(factoryTypefinal.getDisplayname() + " created.").color(TextColor.color(100, 200, 100)));
                                return;
                            }
                        }
                    }
                }
                event.getPlayer().sendMessage(Component.text("Invalid ingredients for a factory. Use /fm to see ingredients.").color(TextColor.color(200, 100, 100)));

            } else if (event.getClickedBlock().getType() == Material.FURNACE) {
                CivWorld cw = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
                CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
                for (FactoryBuild fb : cc.getFactories()) {
                    if (fb.getFurnace().equals(event.getClickedBlock().getLocation())) {
                        Container container = (Container) event.getClickedBlock().getState();
                        if (container.getInventory().contains(Material.CHARCOAL)) {
                            if (fb.isRunning()) {
                                fb.setRunning(false);
                                fb.setRecipeTick(0);
                            } else {
                                if (fb.getCurrentRecipe() == null) {
                                    event.getPlayer().sendMessage(Component.text("No recipe selected").color(TextColor.color(200, 50, 50)));
                                    return;
                                }
                                event.getPlayer().sendMessage(Component.text("Running recipe: " + fb.getCurrentRecipe().getDisplayName()).color(TextColor.color(0, 200, 20)));
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
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getPlayer().getInventory().getItemInMainHand() != null && event.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("HOE"))) {
            //Auto Break crops on right click.
            if (event.getClickedBlock().getType() == WHEAT ||
                    event.getClickedBlock().getType() == MELON_STEM ||
                    event.getClickedBlock().getType() == BEETROOTS ||
                    event.getClickedBlock().getType() == POTATOES ||
                    event.getClickedBlock().getType() == NETHER_WART ||
                    event.getClickedBlock().getType() == CARROTS ||
                    event.getClickedBlock().getType() == PUMPKIN_STEM
            ) {
                CivWorld cw = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
                CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
                CropBlock cb = null;
                if ((cb = cc.getCropAt(event.getClickedBlock().getLocation())) != null) {
                    if (System.currentTimeMillis() - cb.getPlantTime() - cb.getGrowTime() > 0) {
                        Material type = event.getClickedBlock().getType();
                        event.getClickedBlock().breakNaturally();
                        event.getClickedBlock().setType(type);
                        cb.setPlantTime(System.currentTimeMillis());
                        cb.setGrowTime(CivvieAPI.getInstance().getGrowthManager().getGrowthFor(getCropMaterial(event.getClickedBlock().getType()), event.getClickedBlock().getLocation()));
                    }
                    cc.updateCrops();
                }
            }
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getPlayer().getInventory().getItemInMainHand() == null || event.getPlayer().getInventory().getItemInMainHand().getType() == AIR)) {
            CivWorld cw = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
            CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
            CropBlock cb = null;
            if ((cb = cc.getCropAt(event.getClickedBlock().getLocation())) != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Time till fully grown: ").color(TextColor.color(20, 200, 20)).append(Component.text(ItemsUtil.formatTime(cb.getPlantTime() + cb.getGrowTime() - System.currentTimeMillis())).color(TextColor.color(150, 150, 150))));
            }
        }

        if (event.getPlayer().getInventory().getItemInMainHand() != null && CivvieAPI.getInstance().getReinforcelevel().containsKey(event.getPlayer().getInventory().getItemInMainHand().getType())) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {

                PlayerStateManager.ReinforceBlockState state = (PlayerStateManager.ReinforceBlockState) CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(event.getPlayer().getUniqueId(), PlayerStateManager.ReinforceBlockState.class);
                if (state != null) {
                    CivWorld cw = CivvieAPI.getInstance().getWorld(event.getClickedBlock().getWorld().getName());
                    CivChunk cc = cw.getChunkAt(event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());

                    CivBlock cb = cc.getBlockAt(event.getClickedBlock().getLocation());
                    if (CivvieAPI.getInstance().getReinforcelevel().containsKey(event.getPlayer().getInventory().getItemInMainHand().getType())) {
                        if (cb == null) {
                            cb = new CivBlock(cc, event.getClickedBlock().getLocation());
                            cc.addCivBlock(cb);
                            cb.setOwner(state.getReinforceTo());
                            cb.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(event.getPlayer().getInventory().getItemInMainHand().getType()));
                            cb.setReinforcement(cb.getMaxReinforcement());
                            cb.setReinforcedWith(event.getPlayer().getInventory().getItemInMainHand().getType());
                            CivvieAPI.getInstance().playReinforceProtection(cb.getLocation());
                            ItemStack hand = removeOneFromStack(event.getPlayer().getInventory().getItemInMainHand());
                            event.getPlayer().getInventory().setItemInMainHand(hand);
                        } else {
                            if (cb.getOwner() == null || cb.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                if (cb.getOwner() == null)
                                    cb.setOwner(state.getReinforceTo());
                                ItemStack hand = removeOneFromStack(event.getPlayer().getInventory().getItemInMainHand());
                                if (cb.getReinforcedWith() == event.getPlayer().getInventory().getItemInMainHand().getType()) {
                                    if (cb.getReinforcement() < cb.getMaxReinforcement()) {
                                        cb.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                                        cb.setReinforcement(cb.getMaxReinforcement());
                                        cb.setReinforcedWith(event.getPlayer().getInventory().getItemInMainHand().getType());
                                        CivvieAPI.getInstance().playReinforceProtection(cb.getLocation());
                                        event.getPlayer().getInventory().setItemInMainHand(hand);
                                    }
                                } else {
                                    int level = CivvieAPI.getInstance().getReinforcelevel().get(event.getPlayer().getInventory().getItemInMainHand().getType());
                                    cb.setMaxReinforcement(level);
                                    cb.setReinforcement(level);
                                    if (cb.getReinforcedWith() != null)
                                        event.getPlayer().getInventory().addItem(new ItemStack(cb.getReinforcedWith()));
                                    cb.setReinforcedWith(event.getPlayer().getInventory().getItemInMainHand().getType());
                                    event.getPlayer().getInventory().setItemInMainHand(hand);
                                    event.getPlayer().sendMessage("Reinforce to " + cb.getReinforcement());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (event.getEntity() instanceof Player) {
                if (((Player) event.getEntity()).getInventory().getBoots() != null && CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(((Player) event.getEntity()).getInventory().getBoots(), CivvieAPI.getInstance().getEnchantmentManager().raidho)) {
                    int level = CivvieAPI.getInstance().getEnchantmentManager().getLevel(((Player) event.getEntity()).getInventory().getBoots(), CivvieAPI.getInstance().getEnchantmentManager().raidho);
                    if (new Random().nextInt(level + 1) > 0) {
                        event.setCancelled(true);
                        event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_ANVIL_FALL, 1, 0.5f);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDurability(PlayerItemDamageEvent event) {
        if (event.getItem().containsEnchantment(Enchantment.getByKey(CivvieAPI.getInstance().getEnchantmentManager().laguz.getKey()))) {
            int level = event.getItem().getEnchantmentLevel(Enchantment.getByKey(CivvieAPI.getInstance().getEnchantmentManager().laguz.getKey()));
            if (new Random().nextInt(level) != 0) {
                event.setCancelled(true);
            }
        }
    }

    private void openEnchantGUI(Player player) {
        ItemStack is = player.getInventory().getItemInMainHand();
        if (is == null)
            return;
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 27, "Enchanting Table"));

        for (int i = 0; i < 27; i++) {
            ezgui.addCallable(new ItemStack(BLACK_STAINED_GLASS_PANE), (clicker, slot, isShiftClick, isRightClick) -> {
            }, i);
        }

        int i = 0;
        for (GenericEnchant gc : CivvieAPI.getInstance().getEnchantmentManager().getValidEnchantments()) {
            if (gc.canApplyTo(is)) {
                ItemStack book = ItemsUtil.createItem(ENCHANTED_BOOK, gc.getName(), 1, ChatColor.LIGHT_PURPLE + "Meaning: " + gc.getLore());
                ezgui.addCallable(book, (clicker, slot, isShiftClick, isRightClick) -> {
                    ItemStack k = clicker.getInventory().getItemInMainHand();
                    if (k.getType() != is.getType())
                        return;
                    int level = k.getEnchantmentLevel(gc);
                    if (level >= 10) {
                        return;
                    }
                    if (clicker.getLevel() > 0) {
                        k.addUnsafeEnchantment(gc, level + 1);

                        List<Component> lore = new LinkedList<>();
                        if (k.lore() != null)
                            lore.addAll(k.lore());
                        boolean added = false;
                        for (int l = 0; l < lore.size(); l++) {
                            TextComponent lores = (TextComponent) lore.get(l);
                            if (((TextComponent) lores).content().contains(gc.getName())) {
                                lore.set(l, gc.displayName(level + 1));
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            lore = new LinkedList<>();
                            lore.add(gc.displayName(level + 1));
                            if (k.lore() != null)
                                lore.addAll(k.lore());
                        }
                        k.lore(lore);


                        clicker.getInventory().setItemInMainHand(k);
                        clicker.setLevel(clicker.getLevel() - 1);
                        clicker.closeInventory();
                        clicker.getWorld().playSound(clicker.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 0.75f);
                    }
                }, i);
                i++;
            }
        }
        player.openInventory(ezgui.getInventory());

    }

    private Material getCropMaterial(Material type) {
        switch (type) {
            case WHEAT:
                return Material.WHEAT_SEEDS;
            case POTATOES:
                return Material.POTATO;
            case CARROTS:
                return Material.CARROT;
            case BEETROOTS:
                return Material.BEETROOT_SEEDS;
            case PUMPKIN_STEM:
                return Material.PUMPKIN_SEEDS;
            case MELON_STEM:
                return Material.MELON_SEEDS;
        }
        return type;
    }

    private void openFactoryGUI(PlayerInteractEvent event, FactoryBuild fb) {
        EZGUI ezgui = new EZGUI(Bukkit.createInventory(null, 27, fb.getType().getName()));
        int i = 0;
        for (FactoryRecipe fr : fb.getType().getRecipes()) {
            ezgui.addCallable(fr.getIcon(), (player, slot, isShiftClick, isRightClick) -> {
                fb.setCurrentRecipe(fr);
                event.getPlayer().sendMessage(Component.text("Setting recipe to: " + fr.getName()).color(TextColor.color(0, 200, 20)));
            }, i);
            i++;
        }
        event.getPlayer().openInventory(ezgui.getInventory());
    }


    private ItemStack removeOneFromStack(ItemStack is) {
        is.setAmount(is.getAmount() - 1);
        return is;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        for (CombatLogManager.CombatSession combatSession : CivvieAPI.getInstance().getCombatLogManager().getCombatSession(event.getPlayer())) {
            CivvieAPI.getInstance().getCombatLogManager().removeSession(combatSession);
        }


        if (event.getPlayer().getKiller() != null) {
            Player killer = event.getPlayer().getKiller();
            int pearlSlot = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack is = killer.getInventory().getItem(i);
                if (is != null)
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

                PearlManager.PearlData pearldata = CivvieAPI.getInstance().getPearlManager().createPearl(event.getPlayer(), System.currentTimeMillis(), killer.getName(), 1);
                if (killer.getInventory().firstEmpty() == -1) {

                    Entity drop = event.getPlayer().getWorld().dropItem(killer.getLocation(), ItemsUtil.createPrisonPearl(event.getPlayer(), killer, ItemsUtil.formatDate(System.currentTimeMillis()), ItemsUtil.formatDate(pearldata.getLastRefuel()), 20, pearldata.getDesignation()));
                    pearldata.setPearlHolder(new PearlManager.PearlEntityHolder(pearldata, drop));
                } else {
                    killer.getInventory().addItem(ItemsUtil.createPrisonPearl(event.getPlayer(), killer, ItemsUtil.formatDate(System.currentTimeMillis()), ItemsUtil.formatDate(pearldata.getLastRefuel()), 20, pearldata.getDesignation()));
                    pearldata.setPearlHolder(new PearlManager.PearlEntityHolder(pearldata, killer));
                }
                event.getPlayer().kick(Component.text("You have been pearled! Contact your killer (" + killer.getName() + ") on discord and try to negotiate your release, or find friends to free you!").color(TextColor.color(200, 200, 20)));
            }
        }
    }


    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.PLAYER && event.getEntityType() == EntityType.PLAYER) {
            CombatLogManager.CombatSession cs = CivvieAPI.getInstance().getCombatLogManager().getCombatSession((Player) event.getEntity(), (Player) event.getDamager());
            if (cs == null) {
                cs = CivvieAPI.getInstance().getCombatLogManager().createCombatSession((Player) event.getDamager(), (Player) event.getEntity());
                event.getEntity().sendMessage(Component.text("Combat Mode Enabled. Combatlogging will result in death.").color(TextColor.color(255, 50, 50)));
                event.getDamager().sendMessage(Component.text("Combat Mode Enabled. Combatlogging will result in death.").color(TextColor.color(255, 50, 50)));
            }
            cs.setLastTimeCombat(System.currentTimeMillis());
        }


        double pSword = 0;
        double pAxe = 0;
        double pIsa = 0;
        double pArrow = 0;
        double pHagalaz = 0;

        double dUruz = 0;
        double dhagalaz = 0;
        double isa = 0;
        boolean sword = false;
        boolean axe = false;
        if (event.getEntity() instanceof Player) {
            ItemStack[] armor = new ItemStack[]{
                    ((Player) event.getEntity()).getInventory().getHelmet()
                    , ((Player) event.getEntity()).getInventory().getChestplate()
                    , ((Player) event.getEntity()).getInventory().getLeggings()
                    , ((Player) event.getEntity()).getInventory().getBoots()
            };
            for (ItemStack is : armor) {
                if (is != null) {
                    if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(is, CivvieAPI.getInstance().getEnchantmentManager().thurisaz)) {
                        pSword += CivvieAPI.getInstance().getEnchantmentManager().getLevel(is, CivvieAPI.getInstance().getEnchantmentManager().thurisaz);
                    }
                    if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(is, CivvieAPI.getInstance().getEnchantmentManager().algiz)) {
                        pAxe += CivvieAPI.getInstance().getEnchantmentManager().getLevel(is, CivvieAPI.getInstance().getEnchantmentManager().algiz);
                    }
                    if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(is, CivvieAPI.getInstance().getEnchantmentManager().sowelo)) {
                        pIsa += CivvieAPI.getInstance().getEnchantmentManager().getLevel(is, CivvieAPI.getInstance().getEnchantmentManager().sowelo);
                    }
                    if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(is, CivvieAPI.getInstance().getEnchantmentManager().wunjo)) {
                        pArrow += CivvieAPI.getInstance().getEnchantmentManager().getLevel(is, CivvieAPI.getInstance().getEnchantmentManager().wunjo);
                    }
                    if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(is, CivvieAPI.getInstance().getEnchantmentManager().ingus)) {
                        pHagalaz += CivvieAPI.getInstance().getEnchantmentManager().getLevel(is, CivvieAPI.getInstance().getEnchantmentManager().ingus);
                    }
                }
            }
        }
        if (event.getDamager() instanceof Player) {
            ItemStack isk;
            if ((isk = ((Player) event.getDamager()).getInventory().getItemInMainHand()) != null) {
                if (isk.getType().name().contains("_AXE")) {
                    axe = true;
                } else if (isk.getType().name().contains("SWORD")) {
                    sword = true;
                }
                if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(isk, CivvieAPI.getInstance().getEnchantmentManager().uruz)) {
                    dUruz = CivvieAPI.getInstance().getEnchantmentManager().getLevel(isk, CivvieAPI.getInstance().getEnchantmentManager().uruz);
                }
                if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(isk, CivvieAPI.getInstance().getEnchantmentManager().hagalaz)) {
                    dhagalaz = CivvieAPI.getInstance().getEnchantmentManager().getLevel(isk, CivvieAPI.getInstance().getEnchantmentManager().hagalaz);
                }
                if (CivvieAPI.getInstance().getEnchantmentManager().hasEnchantment(isk, CivvieAPI.getInstance().getEnchantmentManager().isa)) {
                    isa = CivvieAPI.getInstance().getEnchantmentManager().getLevel(isk, CivvieAPI.getInstance().getEnchantmentManager().isa);
                }
            }
        }

        double damModifer = 1;
        if (sword) {
            if (pSword > 0)
                damModifer /= (1.0 + (1 / pSword));
        }
        if (axe) {
            if (pAxe > 0)
                damModifer /= (1.0 + (1 / pAxe));
        }
        if (isa > 0) {
            if (pIsa > 0) {
                if (pIsa < isa) {
                    damModifer *= (1.0 + ((isa - pIsa) / (isa + pIsa)) * 0.3);
                }
            } else {
                damModifer *= (1.0 + ((isa) / (isa + 1)) * 0.3);
            }
        }
        if (dhagalaz > 0) {
            if (pHagalaz > 0) {
                if (pHagalaz < dhagalaz) {
                    damModifer *= (1.0 + ((dhagalaz - pHagalaz) / (dhagalaz + pHagalaz)) * 0.3);
                }
            } else {
                damModifer *= (1.0 + ((dhagalaz) / (dhagalaz + 1)) * 0.3);
            }
        }
        if (dUruz > 0) {
            damModifer *= (1.0 + ((dUruz) / (dUruz + 1)) * 0.3);
        }
        event.setDamage(event.getDamage() * damModifer);
    }

    @EventHandler
    public void onItemDestroy(EntityDamageByBlockEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            ItemStack is = ((Item) event.getEntity()).getItemStack();
            if (is.getType() == CACTUS && event.getDamager().getType() == CACTUS) {
                event.setCancelled(true);
            } else if (ItemsUtil.isPrisonPearl(is)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getEntity().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGrow(BlockGrowEvent event) {
        if (event.getNewState().getType() == MELON_STEM ||
                event.getNewState().getType() == MELON ||
                event.getNewState().getType() == PUMPKIN_STEM ||
                event.getNewState().getType() == PUMPKIN ||
                event.getNewState().getType() == CARROTS ||
                event.getNewState().getType() == WHEAT ||
                event.getNewState().getType() == BEETROOTS ||
                event.getNewState().getType() == POTATOES ||
                event.getNewState().getType() == COCOA ||
                event.getNewState().getType() == NETHER_WART
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        CivChunk cc = CivvieAPI.getInstance().getWorld(event.getWorld().getName()).getChunkAt(event.getChunk().getX(), event.getChunk().getZ());
        cc.unload();
        CivvieAPI.getInstance().getWorld(event.getWorld().getName()).removeChunk(cc);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPlayedBefore()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location sp = newSpawn(event.getPlayer());
                    if (sp == null)
                        return;
                    event.getPlayer().getInventory().addItem(CivvieAPI.getInstance().getItemManager().getStarterBook());
                    event.getPlayer().getInventory().addItem(new ItemStack(BREAD, 16));
                    event.getPlayer().teleport(sp);
                    String name = event.getPlayer().getName();
                    NameLayer nameLayer = CivvieAPI.getInstance().getNameLayer(name);
                    if (nameLayer != null) {
                        int i = 1;
                        do {
                            name = event.getPlayer().getName() + i;
                            nameLayer = CivvieAPI.getInstance().getNameLayer(name);
                        } while (nameLayer != null);
                    }
                    CivvieAPI.getInstance().registerNameLayer(nameLayer = new NameLayer(name));
                    nameLayer.getRanks().put(QuickPlayerData.getPlayerData((event.getPlayer()).getUniqueId()), NameLayerRankEnum.OWNER);
                    this.cancel();
                }
            }.runTaskTimer(CivvieAPI.getInstance().getPlugin(), 45, 5);
        }
        if (CivvieAPI.getInstance().getCombatLogManager().getPlayersKilledOffline().contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().getInventory().clear();
            event.getPlayer().setHealth(0);
            CivvieAPI.getInstance().getCombatLogManager().getPlayersKilledOffline().remove(event.getPlayer().getUniqueId());
        }
        List<PlayerStateManager.PlayerState> sentto = CivvieAPI.getInstance().getPlayerStateManager().getPlayerStatesOf(event.getPlayer().getUniqueId(), PlayerStateManager.InviteSentToPlayerState.class);

        for (PlayerStateManager.PlayerState state : sentto) {
            event.getPlayer().sendMessage(Component.text("You have been invited to the group \"" + ((PlayerStateManager.InviteSentToPlayerState) state).getNameLayer().getName() + "\". Click here to join the group.").color(TextColor.color(0, 200, 0)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/nlaccept")));
        }

        boolean foundIP = false;

        long lastLogin = 0;

        for (IPToPlayerManager.IPHolder ipHolder : CivvieAPI.getInstance().getIpToPlayerManager().getIpHolders()) {
            if (event.getPlayer().getAddress().getAddress().getHostAddress().equals(ipHolder.getIp())) {
                if (!ipHolder.getUuids().contains(event.getPlayer().getUniqueId())) {
                    ipHolder.getUuids().add(event.getPlayer().getUniqueId());
                }
                foundIP = true;

                if (lastLogin < ipHolder.getLastLogin()) {
                    lastLogin = ipHolder.getLastLogin();
                }

                for (UUID uuid : ipHolder.getUuids()) {
                    if (CivvieAPI.getInstance().getPearlManager().isPearled(Bukkit.getOfflinePlayer(uuid))) {
                        PearlManager.PearlData pearlData = CivvieAPI.getInstance().getPearlManager().getPearlData(uuid);
                        if (pearlData.updateFuel() || pearlData.getFuel() <= 0.0) {
                            CivvieAPI.getInstance().getPearlManager().freePearl(pearlData);
                        } else {
                            event.getPlayer().kick(Component.text("An alt on this account has been pearled. If you believe this to be a mistake, contact the server owner."));
                        }
                    }
                }
            }
        }

        if (System.currentTimeMillis() - lastLogin > 1000 * 60 * 60 * 24) {
            for (IPToPlayerManager.IPHolder ipHolder : CivvieAPI.getInstance().getIpToPlayerManager().getIpHolders()) {
                if (event.getPlayer().getAddress().getAddress().getHostAddress().equals(ipHolder.getIp())) {
                    ipHolder.setLastLogin(System.currentTimeMillis());
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    event.getPlayer().getInventory().addItem(ItemsUtil.createFuel(1));
                }
            }.runTaskLater(plugin, 5);

        }

        if (!foundIP) {
            IPToPlayerManager.IPHolder ipHolder = new IPToPlayerManager.IPHolder(UUID.randomUUID(), new LinkedList<>(Arrays.asList(event.getPlayer().getUniqueId())), event.getPlayer().getAddress().getAddress().getHostAddress(), System.currentTimeMillis());
            CivvieAPI.getInstance().getIpToPlayerManager().addIPHolder(ipHolder);
            for (UUID uuid : ipHolder.getUuids()) {
                if (CivvieAPI.getInstance().getPearlManager().isPearled(Bukkit.getOfflinePlayer(uuid))) {
                    PearlManager.PearlData pearlData = CivvieAPI.getInstance().getPearlManager().getPearlData(uuid);
                    if (pearlData.updateFuel() || pearlData.getFuel() <= 0) {
                        CivvieAPI.getInstance().getPearlManager().freePearl(pearlData);
                    } else {
                        event.getPlayer().kick(Component.text("An alt on this account has been pearled. If you believe this to be a mistake, contact the server owner."));
                    }
                }
            }
        }
    }

    private Location newSpawn(Player player) {
        long time = System.currentTimeMillis();
        loop:
        for (int tries = 0; tries < 16; tries++) {
            if (System.currentTimeMillis() - time > 1000) {
                return null;
            }
            int x = new Random().nextInt(2 * CivvieAPI.getInstance().WORLD_BOARDER_RADIUS) - CivvieAPI.getInstance().WORLD_BOARDER_RADIUS;
            int z = new Random().nextInt(2 * CivvieAPI.getInstance().WORLD_BOARDER_RADIUS) - CivvieAPI.getInstance().WORLD_BOARDER_RADIUS;
            if ((x * x) + (z * z) > (CivvieAPI.getInstance().WORLD_BOARDER_RADIUS * CivvieAPI.getInstance().WORLD_BOARDER_RADIUS)) {
                tries--;
                continue;
            }
            Block highest = player.getWorld().getBlockAt(x, 200, z);
            yloop:
            for (int y = 180; y >= 0; y--) {
                switch (highest.getType()) {
                    case STONE:
                    case MOSS_BLOCK:
                    case GRASS_BLOCK:
                    case DIRT:
                        highest = highest.getRelative(BlockFace.UP);
                        break yloop;
                    case GRASS:
                        break yloop;
                    case ACACIA_LEAVES:
                    case AZALEA_LEAVES:
                    case BIRCH_LEAVES:
                    case DARK_OAK_LEAVES:
                    case FLOWERING_AZALEA_LEAVES:
                    case JUNGLE_LEAVES:
                    case MANGROVE_LEAVES:
                    case OAK_LEAVES:
                    case SPRUCE_LEAVES:
                    case AIR:
                        highest = highest.getRelative(BlockFace.DOWN);
                        continue yloop;
                    default:
                        continue loop;
                }
            }
            return highest.getLocation();
        }
        return null;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.isBedSpawn() || event.isBedSpawn()) {
            return;
        }
        Location newspawn = newSpawn(event.getPlayer());
        if (newspawn == null) {
            new BukkitRunnable() {
                public void run() {
                    Location temp = newSpawn(event.getPlayer());
                    if (temp != null) {
                        event.getPlayer().teleport(temp);
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 20, 10);
            return;
        }
        event.setRespawnLocation(newspawn);
    }

    private Material[] crops = {Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS, MELON, Material.PUMPKIN};

    @EventHandler
    public void onChunkGenerate(ChunkLoadEvent event) {
        try {
            for (Entity entity : event.getChunk().getEntities()) {
                if (entity.getType() == EntityType.FROG)
                    entity.remove();
            }
        } catch (Exception e) {

        }


        if (event.isNewChunk()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Chunk chunk = event.getWorld().getChunkAt(event.getChunk().getX(), event.getChunk().getZ());
                    chunk.setForceLoaded(true);
                    for (int x = 0; x < 16; x++) {
                        for (int z = 0; z < 16; z++) {
                            for (int y = event.getWorld().getMinHeight(); y < event.getWorld().getMaxHeight(); y++) {
                                if (System.currentTimeMillis() - CivvieAPI.getInstance().getTickManager().getLastTick() > 1300)
                                    return;
                                Block block = chunk.getBlock(x, y, z);
                                if (block.getType().name().endsWith("_ORE")) {
                                    BlockState bs = block.getState();
                                    if (bs.getType().name().contains("DEEPSLATE")) {
                                        bs.setType(Material.DEEPSLATE);
                                    } else {
                                        bs.setType(Material.STONE);
                                    }
                                    bs.update(true, false);
                                }
                            }
                        }
                    }
                    //plugin.getLogger().info("Finished culling ores for chunk \"" + event.getWorld().getName() + "\" " + event.getChunk().getX() + "," + event.getChunk().getZ() + ".");


                    if (new Random().nextInt(2) == 0) {
                        for (int times = 0; times < 40; times++) {
                            int randx = new Random().nextInt(16);
                            int randz = new Random().nextInt(16);
                            Block highest = chunk.getWorld().getHighestBlockAt((chunk.getX() * 16) + randx, (chunk.getZ() * 16) + randz);
                            while (highest.getType() != Material.GRASS_BLOCK && highest.getLocation().getBlockY() > 40) {
                                highest = highest.getRelative(BlockFace.DOWN);
                            }
                            if (highest.getType() == Material.GRASS_BLOCK) {
                                Block b = highest;
                                highest = highest.getRelative(BlockFace.UP);
                                if (!highest.getType().isSolid()) {
                                    BlockState bb1 = b.getState();
                                    bb1.setType(Material.FARMLAND);
                                    bb1.update(true, false);
                                    BlockState bb2 = highest.getState();
                                    bb2.setType(crops[new Random().nextInt(crops.length)]);
                                    bb2.update(true, false);

                                    CivChunk civchunk = CivvieAPI.getInstance().getWorld(chunk.getWorld().getName()).getChunkAt(chunk.getX(), chunk.getZ());
                                    civchunk.addCrop(new CropBlock(civchunk, null, highest.getLocation(), 0, CivvieAPI.getInstance().getGrowthManager().getGrowthFor(getCropMaterial(highest.getType()), highest.getLocation())));
                                    break;
                                }
                                Block finalHighest = highest;
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        if (finalHighest.getLocation().getBlock().getBlockData() instanceof Ageable) {
                                            Ageable ageable = (Ageable) finalHighest.getLocation().getBlock().getBlockData();
                                            ageable.setAge(ageable.getMaximumAge());
                                            finalHighest.getLocation().getBlock().setBlockData(ageable);
                                        }
                                    }
                                }.runTaskLater(CivvieAPI.getInstance().getPlugin(), 1);
                            }
                        }
                    }


                    cancel();
                    chunk.setForceLoaded(false);
                }
            }.runTaskTimer(plugin, 1, 1);
        }
        CivChunk.load(event.getChunk().getX(), event.getChunk().getZ(), CivvieAPI.getInstance().getWorld(event.getWorld().getName()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        CivWorld cw = CivvieAPI.getInstance().getWorld(event.getPlayer().getWorld().getName());
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                CivChunk chunk = cw.getChunkAt(event.getPlayer().getChunk().getX() + x, event.getPlayer().getChunk().getZ() + z);
                if (chunk != null) {
                    for (Snitch jb : chunk.getJukeblocks()) {
                        if (jb.getLocation().distanceSquared(event.getTo()) < jb.getRadius() * jb.getRadius()) {
                            CivBlock civBlock = chunk.getBlockAt(jb.getLocation());
                            if (civBlock != null) {
                                boolean found = false;
                                for (PlayerStateManager.PlayerState playerstate : CivvieAPI.getInstance().getPlayerStateManager().getPlayerStatesOf(event.getPlayer().getUniqueId(), PlayerStateManager.TriggerMoveJukeAlertState.class)) {
                                    if (playerstate instanceof PlayerStateManager.TriggerMoveJukeAlertState) {
                                        if (((PlayerStateManager.TriggerMoveJukeAlertState) playerstate).getJukebox().equals(jb.getLocation())) {
                                            found = true;
                                            break;
                                        }
                                    }
                                }
                                if (!found) {
                                    if (civBlock.getOwner() != null)
                                        if (civBlock.getOwner().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                                            PlayerStateManager.TriggerMoveJukeAlertState trigger = new PlayerStateManager.TriggerMoveJukeAlertState(event.getPlayer().getUniqueId(), jb.getLocation());
                                            CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(trigger);

                                            Snitch.JukeRecord jr = new Snitch.PlayerEnterJukeRecord(System.currentTimeMillis(), jb, QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()));
                                            jb.addJukeRecord(jr);
                                            jr.onCall(civBlock);
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onAsyncChatEvent(AsyncChatEvent event) {
        event.setCancelled(true);
        PlayerStateManager.InviteMemberPlayerChatState invitestate = (PlayerStateManager.InviteMemberPlayerChatState) CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(event.getPlayer().getUniqueId(), PlayerStateManager.InviteMemberPlayerChatState.class);
        if (invitestate != null) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(((TextComponent) event.originalMessage()).content());
            PlayerStateManager.InviteSentToPlayerState sentto = new PlayerStateManager.InviteSentToPlayerState(player.getUniqueId(), invitestate.getNameLayer(), invitestate.getInvitedRank());
            event.getPlayer().sendMessage(Component.text("Invite sent to " + player.getName()));
            CivvieAPI.getInstance().getPlayerStateManager().addPlayerState(sentto);
            CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(invitestate);
            if (player.isOnline()) {
                ((Player) player).sendMessage(Component.text("You have been invited to the group \"" + invitestate.getNameLayer().getName() + "\". Click here to join the group.").color(TextColor.color(0, 200, 0)).clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/nlaccept")));
            }
            return;
        }


        PlayerStateManager.NameLayerChatState chat = (PlayerStateManager.NameLayerChatState) CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(event.getPlayer().getUniqueId(), PlayerStateManager.NameLayerChatState.class);

        String message = ((TextComponent) event.originalMessage()).content();
        if (chat != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (chat.getNameLayer().getRanks().containsKey(QuickPlayerData.getPlayerData(player.getUniqueId())))
                    player.sendMessage(Component.text("[" + chat.getNameLayer().getName() + "] ").color(TextColor.color(100, 100, 100)).append(Component.text(event.getPlayer().getName() + ": " + message).color(TextColor.color(200, 200, 200))));
            }
            CivvieAPI.getInstance().getPlugin().getLogger().info("> [" + chat.getNameLayer().getName() + "]" + event.getPlayer().getName() + ": " + message);
        } else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(Component.text("[!] ").color(TextColor.color(100, 100, 100)).append(Component.text(event.getPlayer().getName() + ": " + message).color(TextColor.color(200, 200, 200))));
            }
            CivvieAPI.getInstance().getPlugin().getLogger().info("> [!] " + event.getPlayer().getName() + ": " + message);
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getEntity().getItemStack())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getEntity().getItemStack());
            if (pearlData != null) {
                pearlData.setPearlHolder(new PearlManager.PearlEntityHolder(pearlData, event.getEntity()));
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getItem().getItemStack())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getItem().getItemStack());
            if (pearlData != null) {
                pearlData.setPearlHolder(new PearlManager.PearlEntityHolder(pearlData, event.getPlayer()));
            }
        }
    }

    @EventHandler
    public void onHopperPick(InventoryPickupItemEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getItem().getItemStack())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getItem().getItemStack());
            if (pearlData != null) {
                if (event.getInventory().getHolder() instanceof Container) {
                    pearlData.setPearlHolder(new PearlManager.PearlBlockHolder(pearlData, ((Container) event.getInventory().getHolder()).getLocation()));
                } else {
                    CivvieAPI.getInstance().getPlugin().getLogger().info("Failed to pickup Pearl from " + event.getInventory().getHolder().getClass().getName());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getItemDrop().getItemStack())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getItemDrop().getItemStack());
            if (pearlData != null) {
                pearlData.setPearlHolder(new PearlManager.PearlEntityHolder(pearlData, event.getItemDrop()));
            }
        }
    }

    @EventHandler
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (ItemsUtil.isPrisonPearl(event.getItem())) {
            PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(event.getItem());
            if (pearlData != null) {
                if (event.getDestination().getHolder() instanceof Container) {
                    pearlData.setPearlHolder(new PearlManager.PearlBlockHolder(pearlData, ((Container) event.getDestination().getHolder()).getLocation()));
                } else {
                    CivvieAPI.getInstance().getPlugin().getLogger().info("Failed to pickup Pearl from " + event.getDestination().getHolder().getClass().getName());
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (CivvieAPI.getInstance().getCombatLogManager().getCombatSession(event.getPlayer()).size() > 0) {
            CivvieAPI.getInstance().getPlugin().getLogger().info("Player " + event.getPlayer().getName() + " combat logged (data=" + CivvieAPI.getInstance().getCombatLogManager().getCombatSession(event.getPlayer()).size() + ")");
            for (ItemStack is : event.getPlayer().getInventory().getContents()) {
                if (is != null) {
                    if (ItemsUtil.isPrisonPearl(is)) {
                        Entity item = event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), is);
                        PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(is);
                        pearlData.setPearlHolder(new PearlManager.PearlEntityHolder(pearlData, item));
                    } else {
                        Entity item = event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), is);
                    }
                }
            }
            CivvieAPI.getInstance().getCombatLogManager().getPlayersKilledOffline().add(event.getPlayer().getUniqueId());
            return;
        }
        for (ItemStack is : event.getPlayer().getInventory().getContents()) {
            if (is != null) {
                if (ItemsUtil.isPrisonPearl(is)) {
                    Entity item = event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), is);
                    PearlManager.PearlData pearlData = ItemsUtil.getPearledPlayerFromPearl(is);
                    pearlData.setPearlHolder(new PearlManager.PearlEntityHolder(pearlData, item));
                    event.getPlayer().getInventory().remove(is);
                }
            }
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        event.setExpToDrop(0);
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            if (event.getCaught() instanceof Item) {
                if (((Item) event.getCaught()).getItemStack().getType() == ENCHANTED_BOOK) {
                    event.getCaught().remove();
                }
            }
        }
    }

    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent event) {
        if (event.getEnchanter().getExpToLevel() <= 30) {
            event.getEnchanter().setLevel(0);
        } else {
            event.getEnchanter().setLevel(event.getEnchanter().getLevel() - 30);
        }
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        if (event.getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType() == GOLD_BLOCK) {
            Block gold = event.getPlayer().getLocation().getBlock();
            do {
                gold = gold.getRelative(BlockFace.UP);
            } while (gold.getType() != GOLD_BLOCK && gold.getLocation().getBlockY() < gold.getWorld().getMaxHeight());
            if (gold.getLocation().getBlockY() <= gold.getWorld().getMaxHeight()) {
                gold = gold.getRelative(BlockFace.UP);
                event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                event.getPlayer().teleport(gold.getLocation().add(0.5, 0.01, 0.5));
                event.getPlayer().getWorld().playSound(gold.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }

    @EventHandler
    public void onJump(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            if (event.getPlayer().getLocation().subtract(0, 1, 0).getBlock().getType() == GOLD_BLOCK) {
                Block gold = event.getPlayer().getLocation().getBlock();
                do {
                    gold = gold.getRelative(BlockFace.DOWN);
                } while (gold.getType() != GOLD_BLOCK && gold.getLocation().getBlockY() > gold.getWorld().getMinHeight());
                if (gold.getLocation().getBlockY() >= gold.getWorld().getMinHeight()) {
                    gold = gold.getRelative(BlockFace.DOWN);
                    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                    event.getPlayer().teleport(gold.getLocation().add(0.5, 0.01, 0.5));
                    event.getPlayer().getWorld().playSound(gold.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        CivWorld world = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        if (world != null) {

            for (BastionField bf : world.getBastionFields()) {
                if (bf.getBastionBlock().distanceSquared(event.getBlock().getLocation()) <= bf.getRadius() * bf.getRadius()) {
                    if (!bf.getNameLayer().getRanks().containsKey(QuickPlayerData.getPlayerData(event.getPlayer().getUniqueId()))) {
                        event.setCancelled(true);
                        event.getPlayer().getWorld().playSound(event.getBlock().getLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1f, 1.2f);
                        return;
                    }
                }
            }


            CivChunk chunk = world.getChunkAt(event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
            if (chunk != null) {
                Material type = event.getBlockPlaced().getType();

                PlayerStateManager.ReinforceBlockState state = (PlayerStateManager.ReinforceBlockState) CivvieAPI.getInstance().getPlayerStateManager().getPlayerStateOf(event.getPlayer().getUniqueId(), PlayerStateManager.ReinforceBlockState.class);

                if (state == null) {
                    if (event.getItemInHand().hasItemMeta() && event.getItemInHand().getItemMeta().hasLore())
                        if (event.getItemInHand().getItemMeta().getLore().contains(ItemsUtil.CITYBASTION) || event.getItemInHand().getItemMeta().getLore().contains(ItemsUtil.VAULTBASTION)) {
                            event.setCancelled(true);
                            event.getPlayer().sendMessage(Component.text("You need to reinforce the block to place it."));
                            return;
                        }


                    if (event.getBlockPlaced().getType() == Material.STONE) {
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        if (cb != null) {
                            chunk.addCivBlock(cb);
                            cb.setOwner(null);
                            cb.setMaxReinforcement(-1);
                            cb.setReinforcement(-1);
                        }
                    }
                    if (event.getBlockPlaced().getType() == Material.DEEPSLATE) {
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        if (cb != null) {
                            chunk.addCivBlock(cb);
                            cb.setOwner(null);
                            cb.setMaxReinforcement(-1);
                            cb.setReinforcement(-1);
                        }
                    }
                    if (event.getBlockPlaced().getType() == NETHERRACK) {
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        if (cb != null) {
                            chunk.addCivBlock(cb);
                            cb.setOwner(null);
                            cb.setMaxReinforcement(-1);
                            cb.setReinforcement(-1);
                        }
                    }
                    switch (type) {
                        case BEETROOT_SEEDS:
                        case BEETROOTS:
                        case MELON_SEEDS:
                        case PUMPKIN_SEEDS:
                        case WHEAT_SEEDS:
                        case WHEAT:
                        case POTATOES:
                        case CARROTS:
                        case NETHER_WART:
                        case ACACIA_SAPLING:
                        case BIRCH_SAPLING:
                        case JUNGLE_SAPLING:
                        case SPRUCE_SAPLING:
                        case OAK_SAPLING:
                        case DARK_OAK_SAPLING:
                        case MANGROVE_PROPAGULE:
                            CivBlock civBlock = chunk.getBlockAt(event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                            if (civBlock == null) {
                                civBlock = new CivBlock(chunk, event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                            }
                            long growtime = CivvieAPI.getInstance().getGrowthManager().getGrowthFor(type, event.getBlockPlaced().getLocation());
                            CropBlock cp = new CropBlock(chunk, civBlock, event.getBlockPlaced().getLocation(), System.currentTimeMillis(), growtime);
                            chunk.addCivBlock(cp);
                            chunk.addCrop(cp);
                            cp.setOwner(null);
                            cp.setMaxReinforcement(-1);
                            cp.setReinforcement(-1);
                            break;
                        default:
                            break;
                    }
                    return;
                }
                Material reinfmat = state.getReinforce();
                if (!event.getPlayer().getInventory().contains(reinfmat)) {
                    event.setCancelled(true);
                    CivvieAPI.getInstance().getPlayerStateManager().removePlayerState(state);
                    event.getPlayer().sendMessage(Component.text("You no longer have any more reinforcable materials. Stopping reinforcement."));
                    return;
                }

                if (event.getPlayer().getInventory().contains(state.getReinforce())) {
                    ItemsUtil.removeItem(state.getReinforce(), 1, event.getPlayer());
                }


                switch (type) {
                    case BEETROOT_SEEDS:
                    case BEETROOTS:
                    case MELON_SEEDS:
                    case PUMPKIN_SEEDS:
                    case WHEAT_SEEDS:
                    case WHEAT:
                    case POTATOES:
                    case CARROTS:
                    case NETHER_WART:
                    case ACACIA_SAPLING:
                    case BIRCH_SAPLING:
                    case JUNGLE_SAPLING:
                    case SPRUCE_SAPLING:
                    case OAK_SAPLING:
                    case DARK_OAK_SAPLING:
                    case MANGROVE_PROPAGULE:
                        CivBlock civBlock = chunk.getBlockAt(event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        if (civBlock == null) {
                            civBlock = new CivBlock(chunk, event.getBlockPlaced().getRelative(BlockFace.DOWN).getLocation());
                        }
                        CropBlock cp = new CropBlock(chunk, civBlock, event.getBlockPlaced().getLocation(), System.currentTimeMillis(), CivvieAPI.getInstance().getGrowthManager().getGrowthFor(type, event.getBlockPlaced().getLocation()));
                        chunk.addCivBlock(cp);
                        chunk.getCropBlocks().add(cp);
                        cp.setOwner(state.getReinforceTo());
                        cp.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                        cp.setReinforcement(cp.getMaxReinforcement());
                        cp.setReinforcedWith(state.getReinforce());
                        CivvieAPI.getInstance().playReinforceProtection(cp.getLocation());
                        break;
                    case ENDER_CHEST:
                        if (event.getItemInHand().getItemMeta().getLore().contains(ItemsUtil.VAULTBASTION)) {
                            CivBlock bastionBlock = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                            bastionBlock.setOwner(state.getReinforceTo());
                            bastionBlock.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                            bastionBlock.setReinforcement(bastionBlock.getMaxReinforcement());
                            bastionBlock.setReinforcedWith(state.getReinforce());
                            chunk.addCivBlock(bastionBlock);
                            chunk.getWorld().addBastion(new BastionField(event.getBlockPlaced().getLocation(), 9, state.getReinforceTo()));
                        } else if (event.getItemInHand().getItemMeta().getLore().contains(ItemsUtil.CITYBASTION)) {
                            CivBlock bastionBlock = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                            bastionBlock.setOwner(state.getReinforceTo());
                            bastionBlock.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                            bastionBlock.setReinforcement(bastionBlock.getMaxReinforcement());
                            bastionBlock.setReinforcedWith(state.getReinforce());
                            chunk.addCivBlock(bastionBlock);
                            chunk.getWorld().addBastion(new BastionField(event.getBlockPlaced().getLocation(), 51, state.getReinforceTo()));
                        } else {
                            event.setCancelled(true);
                        }
                        break;
                    case JUKEBOX:
                        CivBlock juke = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(juke);
                        juke.setOwner(state.getReinforceTo());
                        juke.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                        juke.setReinforcement(juke.getMaxReinforcement());
                        juke.setReinforcedWith(state.getReinforce());
                        CivvieAPI.getInstance().playReinforceProtection(juke.getLocation());
                        chunk.addJukeBlock(new Snitch(event.getBlock().getLocation(), 5, Snitch.JukeType.JUKEBOX));
                        break;
                    case NOTE_BLOCK:
                        CivBlock note = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(note);
                        note.setOwner(state.getReinforceTo());
                        note.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                        note.setReinforcement(note.getMaxReinforcement());
                        note.setReinforcedWith(state.getReinforce());
                        CivvieAPI.getInstance().playReinforceProtection(note.getLocation());
                        chunk.addJukeBlock(new Snitch(event.getBlock().getLocation(), 5, Snitch.JukeType.NOTEBLOCK));
                        break;
                    case OBSIDIAN:
                    case CRYING_OBSIDIAN:
                        CivBlock obs = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(obs);
                        obs.setOwner(state.getReinforceTo());
                        obs.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()) / 15);
                        obs.setReinforcement(obs.getMaxReinforcement());
                        obs.setReinforcedWith(state.getReinforce());
                        CivvieAPI.getInstance().playReinforceProtection(obs.getLocation());
                        break;
                    default:
                        CivBlock cb = new CivBlock(chunk, event.getBlockPlaced().getLocation());
                        chunk.addCivBlock(cb);
                        cb.setOwner(state.getReinforceTo());
                        cb.setMaxReinforcement(CivvieAPI.getInstance().getReinforcelevel().get(state.getReinforce()));
                        cb.setReinforcement(cb.getMaxReinforcement());
                        cb.setReinforcedWith(state.getReinforce());
                        CivvieAPI.getInstance().playReinforceProtection(cb.getLocation());
                        break;
                }
            }
        }
    }


    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        CivWorld civWorld = CivvieAPI.getInstance().getWorld(event.getLocation().getWorld().getName());
        for (Block block : new LinkedList<>(event.blockList())) {
            CivChunk cc = civWorld.getChunkAt(block.getChunk().getX(), block.getChunk().getZ());
            CivBlock cb = null;
            if (cc != null && ((cb = cc.getBlockAt(block.getLocation())) != null)) {
                cb.setReinforcement((int) (cb.getReinforcement() - event.getYield() - 1));
                if (cb.getReinforcement() > 0) {
                    event.blockList().remove(block);
                } else {
                    cc.removeCivBlock(cb);
                }
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        CivWorld civWorld = CivvieAPI.getInstance().getWorld(event.getBlock().getWorld().getName());
        for (Block block : new LinkedList<>(event.blockList())) {
            CivChunk cc = civWorld.getChunkAt(block.getChunk().getX(), block.getChunk().getZ());
            CivBlock cb = null;
            if (cc != null && ((cb = cc.getBlockAt(block.getLocation())) != null)) {
                cb.setReinforcement((int) (cb.getReinforcement() - event.getYield() - 1));
                if (cb.getReinforcement() > 0) {
                    event.blockList().remove(block);
                } else {
                    cc.removeCivBlock(cb);
                }
            }
        }
    }

    @EventHandler
    public void onFertilize(BlockFertilizeEvent event) {
        if (event.getBlock().getType() == GRASS_BLOCK ||
                event.getBlock().getType() == CRIMSON_NYLIUM ||
                event.getBlock().getType() == WARPED_NYLIUM ||
                event.getBlock().getType() == PODZOL ||
                event.getBlock().getType() == MOSS_BLOCK ||
                event.getBlock().getType() == GRASS ||
                event.getBlock().getType() == FERN
        ) {
            //TODO: Invert this so it does not create an empty if.
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB)
            event.setCancelled(true);
    }
}
