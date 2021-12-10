package com.kotori316.limiter;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.command.LMSCommand;
import com.kotori316.limiter.command.TestSpawnArgument;

@Mod(LimitMobSpawn.MOD_ID)
public class LimitMobSpawn {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String MOD_ID = "limitmobspawn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Level LOG_LEVEL = Boolean.getBoolean("limit-mob-spawn") ? Level.DEBUG : Level.TRACE;

    public LimitMobSpawn() {
        //Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(LMSHandler::registerCapability);
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.getInstance().setup(common));
    }

    public void setup(FMLCommonSetupEvent event) {
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

    public static SpawnCheckResult allowSpawning(BlockGetter worldIn, BlockPos pos,
                                                 EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        LazyOptional<LMSHandler> maybeHandler = worldIn instanceof ICapabilityProvider ? ((ICapabilityProvider) worldIn).getCapability(Caps.getLmsCapability()) : LazyOptional.empty();

        boolean matchForce = LMSHandler.getCombinedForce(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).filter(TestSpawn::isDeterministic)
            .anyMatch(spawn -> spawn.test(worldIn, pos, entityTypeIn, reason));
        if (matchForce) return SpawnCheckResult.FORCE;
        boolean matchDefault = LMSHandler.getCombinedDefault(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).filter(TestSpawn::isDeterministic)
            .anyMatch(spawn -> spawn.test(worldIn, pos, entityTypeIn, reason));
        if (matchDefault) return SpawnCheckResult.DEFAULT;
        boolean matchDeny = LMSHandler.getCombinedDeny(SpawnConditionLoader.INSTANCE.getHolder(), maybeHandler).filter(TestSpawn::isDeterministic)
            .anyMatch(spawn -> spawn.test(worldIn, pos, entityTypeIn, reason));
        if (matchDeny) return SpawnCheckResult.DENY;
        else return SpawnCheckResult.DEFAULT;
    }

    public enum SpawnCheckResult {
        DENY, DEFAULT, FORCE
    }
}
