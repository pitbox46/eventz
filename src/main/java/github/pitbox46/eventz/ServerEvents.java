package github.pitbox46.eventz;

import com.mojang.authlib.GameProfile;
import github.pitbox46.eventz.commands.ModCommands;
import github.pitbox46.eventz.data.ActiveEvent;
import github.pitbox46.eventz.data.Condition;
import github.pitbox46.eventz.data.EventRegistration;
import github.pitbox46.eventz.data.contestant.EventContestant;
import github.pitbox46.eventz.network.PacketHandler;
import github.pitbox46.eventz.network.server.SStopBoundary;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.AnimalTameEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.loading.FileUtils;
import net.minecraftforge.fml.network.PacketDistributor;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

import static net.minecraftforge.eventbus.api.EventPriority.LOWEST;

public class ServerEvents {
    public static long tick = 0;
    public static FakePlayer fakePlayer;
    private static long previousEventTime = 0;
    private static int cooldown;

    public static void onEventStop() {
        previousEventTime = System.currentTimeMillis() / 6000;
        calculateCooldown();
        Eventz.activeEvent = null;
        Scoreboard scoreboard = Eventz.getServer().getScoreboard();
        if (scoreboard.getObjectiveInDisplaySlot(1) != null) {
            scoreboard.removeObjective(Objects.requireNonNull(scoreboard.getObjectiveInDisplaySlot(1)));
        }
        PacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new SStopBoundary());
    }

    public static void calculateCooldown() {
        cooldown = Config.LOWER_COOLDOWN.get() + Eventz.RANDOM.nextInt(Config.UPPER_COOLDOWN.get() - Config.LOWER_COOLDOWN.get());
    }

    public static void startRandomEvent() {
        if (!EventRegistration.EVENTS.isEmpty()) {
            int eventIndex = Eventz.RANDOM.nextInt(EventRegistration.EVENTS.size());
            String eventKey = EventRegistration.EVENTS.keySet().stream().skip(eventIndex).findFirst().orElseThrow(IndexOutOfBoundsException::new);
            Eventz.activeEvent = new ActiveEvent(EventRegistration.EVENTS.get(eventKey));
            Eventz.activeEvent.start(Eventz.getServer().getPlayerList());
        }
    }

    public static void startSpecificEvent(String key) {
        Eventz.activeEvent = new ActiveEvent(EventRegistration.EVENTS.get(key));
        Eventz.activeEvent.start(Eventz.getServer().getPlayerList());
    }

    public static void sendGlobalMsg(ITextComponent msg) {
        Eventz.getServer().getPlayerList().func_232641_a_(msg, ChatType.CHAT, Util.DUMMY_UUID);
        MinecraftForge.EVENT_BUS.post(new ServerChatEvent(fakePlayer, msg.getUnformattedComponentText(), null));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        Path modFolder = event.getServer().func_240776_a_(new FolderName("eventz"));
        FileUtils.getOrCreateDirectory(modFolder, "eventz");
        EventRegistration.register(modFolder.toFile());
        Eventz.setServer(event.getServer());
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent tickEvent) {
        if (tickEvent.phase == TickEvent.Phase.END) {
            if (fakePlayer == null) {
                fakePlayer = new FakePlayer(Eventz.getServer().getWorlds().iterator().next(), new GameProfile(Util.DUMMY_UUID, "Eventz"));
            }
            if (Eventz.activeEvent == null && Config.UPPER_COOLDOWN.get() - Config.LOWER_COOLDOWN.get() > 0) {
                if (previousEventTime == 0) {
                    previousEventTime = System.currentTimeMillis() / 6000;
                    calculateCooldown();
                } else if (previousEventTime + cooldown < System.currentTimeMillis() / 6000) {
                    startRandomEvent();
                }
            }
            if (Eventz.activeEvent != null) {
                Eventz.activeEvent.tick();
            }
            tick++;
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (tick % 20 == 11 && event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END && Eventz.activeEvent != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.player;
            EventContestant contestant = Eventz.activeEvent.getContestant(player);

            if (contestant != null && player.isAlive()) {
                if (contestant.hasUnfilledCondition("test_player_tick")) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName
                    Eventz.activeEvent.trigger(contestant, "test_player_tick", player.getUniqueID().toString(), player.getGameProfile().getName());
                }
                if (contestant.hasUnfilledCondition("area_check")) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, double posX, double posY, double posZ, String biome, boolean inVillage
                    Eventz.activeEvent.trigger(contestant, "area_check", player.getUniqueID().toString(), player.getGameProfile().getName(), player.getPosX(), player.getPosY(), player.getPosZ(), player.getEntityWorld().getBiome(player.getPosition()).toString(), player.getServerWorld().isVillage(player.getPosition()));
                }
                if (contestant.hasUnfilledCondition("held_item_check")) {
                    ItemStack main = player.getHeldItemMainhand();
                    ItemStack off = player.getHeldItemOffhand();
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String mainhandItem, int mainhandItemCount, String offhandItem, int offhandItemCount
                    Eventz.activeEvent.trigger(contestant, "held_item_check", player.getUniqueID().toString(), player.getGameProfile().getName(), main.getItem().toString(), main.getCount(), off.getItem().toString(), off.getCount());
                }
                if (contestant.hasUnfilledCondition("exp_check")) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, int expLevel
                    Eventz.activeEvent.trigger(contestant, "exp_check", player.getUniqueID().toString(), player.getGameProfile().getName(), player.experienceLevel);
                }
                if (contestant.hasUnfilledCondition("potion_effect_check")) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String[] effects
                    Collection<EffectInstance> effects = player.getActivePotionEffects();
                    Eventz.activeEvent.trigger(contestant, "potion_effect_check", player.getUniqueID().toString(), player.getGameProfile().getName(), effects.stream().map(effect -> effect.getEffectName() + '%' + effect.getAmplifier() + '%' + effect.getDuration()).toArray(String[]::new));
                }
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getTrueSource() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getSource().getTrueSource();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("kill_entity")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String entityType, String entityName, int entityID
                Eventz.activeEvent.trigger(contestant, "kill_entity", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getEntityLiving().getEntityString(), event.getEntityLiving().getName().getString(), event.getEntityLiving().getEntityId());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onEntityDamaged(LivingDamageEvent event) {
        if (event.getSource().getTrueSource() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getSource().getTrueSource();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("damage_entity")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String entityType, String entityName, int entityID, float amount, boolean willDie
                Eventz.activeEvent.trigger(contestant, "damage_entity", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getEntityLiving().getEntityString(), event.getEntityLiving().getName().getString(), event.getEntityLiving().getEntityId(), event.getAmount(), event.getEntityLiving().getHealth() <= event.getAmount());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onTameMob(AnimalTameEvent event) {
        if (event.getTamer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getTamer();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("tame_mob")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String entityType
                Eventz.activeEvent.trigger(contestant, "tame_mob", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getAnimal().getEntityString());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onBabySpawn(BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getCausedByPlayer();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("breed_mob")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String entityType
                Eventz.activeEvent.trigger(contestant, "breed_mob", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getParentA().getEntityString());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getPlayer() instanceof ServerPlayerEntity && event.getPlayer().getHeldItem(event.getHand()).getItem() instanceof BucketItem && ((BucketItem) event.getPlayer().getHeldItem(event.getHand()).getItem()).getFluid().getClass() == EmptyFluid.class) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("milk_mob")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String entityType
                Eventz.activeEvent.trigger(contestant, "milk_mob", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getTarget().getEntityString());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onFish(ItemFishedEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            EventContestant contestant;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant(player)) != null && contestant.hasUnfilledCondition("fish")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String[] drops
                Eventz.activeEvent.trigger(contestant, "fish", (player).getUniqueID().toString(), (player).getGameProfile().getName(), event.getDrops().stream().map(stack -> stack.getCount() + "%" + stack.getItem().getRegistryName()).toArray());
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ItemStack crafted = event.getCrafting();
            EventContestant contestant = null;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant((ServerPlayerEntity) event.getPlayer())) != null && contestant.hasUnfilledCondition("item_crafted")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String item, int itemCount
                Eventz.activeEvent.trigger(contestant, "item_crafted", event.getPlayer().getUniqueID().toString(), event.getPlayer().getGameProfile().getName(), crafted.getItem().getRegistryName().toString(), crafted.getCount());
            }
            if (contestant != null && contestant.hasUnfilledCondition("random_craft")) {
                Condition condition = contestant.conditions.get("random_craft").getLeft();
                if (ItemStack.areItemStacksEqual(condition.recipe.getRecipeOutput(), crafted)) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName
                    Eventz.activeEvent.trigger(contestant, "random_craft", event.getPlayer().getUniqueID().toString(), event.getPlayer().getGameProfile().getName(), crafted.getItem().getRegistryName().toString(), crafted.getCount());
                }
            }
        }
    }

    @SubscribeEvent(priority = LOWEST)
    public void onItemSmelted(PlayerEvent.ItemSmeltedEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ItemStack crafted = event.getSmelting();
            EventContestant contestant = null;
            if (Eventz.activeEvent != null && (contestant = Eventz.activeEvent.getContestant((ServerPlayerEntity) event.getPlayer())) != null && contestant.hasUnfilledCondition("item_smelted")) {
                //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName, String item, int itemCount
                Eventz.activeEvent.trigger(contestant, "item_smelted", event.getPlayer().getUniqueID().toString(), event.getPlayer().getGameProfile().getName(), crafted.getItem().getRegistryName().toString(), crafted.getCount());
            }
            if (contestant != null && contestant.hasUnfilledCondition("random_smelt")) {
                Condition condition = contestant.conditions.get("random_smelt").getLeft();
                if (ItemStack.areItemStacksEqual(condition.recipe.getRecipeOutput(), crafted)) {
                    //JSObject previousValues, JSObject globalData, String contestantName, String uuid, String playerName
                    Eventz.activeEvent.trigger(contestant, "random_smelt", event.getPlayer().getUniqueID().toString(), event.getPlayer().getGameProfile().getName(), crafted.getItem().getRegistryName().toString(), crafted.getCount());
                }
            }
        }
    }
}
