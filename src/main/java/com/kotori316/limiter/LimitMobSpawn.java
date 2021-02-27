package com.kotori316.limiter;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.command.LMSCommand;
import com.kotori316.limiter.command.TestSpawnArgument;

@Mod(LimitMobSpawn.MOD_ID)
public class LimitMobSpawn {
    public static final String MOD_ID = "limit-mob-spawn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Level LOG_LEVEL = Boolean.getBoolean("limit-mob-spawn") ? Level.DEBUG : Level.TRACE;

    public LimitMobSpawn() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    public void setup(FMLCommonSetupEvent event) {
        LMSHandler.registerCapability();
        TestSpawnArgument.registerArgumentType();
    }

    @SubscribeEvent
    public void addLister(AddReloadListenerEvent event) {
        event.addListener(SpawnConditionLoader.INSTANCE);
    }

    @SubscribeEvent
    public void addCommand(RegisterCommandsEvent event) {
        LMSCommand.register(event.getDispatcher());
    }

    /**
     * The last guard to prevent mob spawning.
     * This method is for initial spawning of Structures.
     */
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // MISCs(item frame, ender pearl, potion cloud, etc) should be allowed to be spawned.
        // Boss monsters(Ender Dragon, Wither) should be spawned.
        if (event.getEntity().getType().getClassification() == EntityClassification.MISC || !event.getEntity().isNonBoss())
            return;
        LazyOptional<LMSHandler> maybeHandler = event.getWorld().getCapability(Caps.getLmsCapability());

        if (LMSHandler.getCombinedForce(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn1 -> spawn1.test(event.getWorld(), event.getEntity().getPosition(), event.getEntity().getType(), null)) ||
            LMSHandler.getCombinedDefault(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn -> spawn.test(event.getWorld(), event.getEntity().getPosition(), event.getEntity().getType(), null)))
            return; // SKIP
        if (LMSHandler.getCombinedDeny(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn1 -> spawn1.test(event.getWorld(), event.getEntity().getPosition(), event.getEntity().getType(), null))) {
            LOGGER.log(LOG_LEVEL, "onEntityJoinWorld denied spawning of {}.", event.getEntity());
            event.setCanceled(true);
        }
    }

    public static SpawnCheckResult allowSpawning(IBlockReader worldIn, BlockPos pos,
                                                 EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        LazyOptional<LMSHandler> maybeHandler = worldIn instanceof World ? ((World) worldIn).getCapability(Caps.getLmsCapability()) : LazyOptional.empty();

        boolean matchForce = LMSHandler.getCombinedForce(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn11 -> spawn11.test(worldIn, pos, entityTypeIn, reason));
        if (matchForce) return SpawnCheckResult.FORCE;
        boolean matchDefault = LMSHandler.getCombinedDefault(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn1 -> spawn1.test(worldIn, pos, entityTypeIn, reason));
        if (matchDefault) return SpawnCheckResult.DEFAULT;
        boolean matchDeny = LMSHandler.getCombinedDeny(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).anyMatch(spawn -> spawn.test(worldIn, pos, entityTypeIn, reason));
        if (matchDeny) return SpawnCheckResult.DENY;
        else return SpawnCheckResult.DEFAULT;
    }

    public enum SpawnCheckResult {
        DENY, DEFAULT, FORCE
    }
}
