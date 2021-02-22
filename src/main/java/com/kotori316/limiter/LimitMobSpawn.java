package com.kotori316.limiter;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(LimitMobSpawn.MOD_ID)
public class LimitMobSpawn {
    public static final String MOD_ID = "limit-mob-spawn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static Set<TestSpawn> denySet = new HashSet<>();
    public static Set<TestSpawn> defaultSet = new HashSet<>();
    public static Set<TestSpawn> forceSet = new HashSet<>();

    public LimitMobSpawn() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void addLister(AddReloadListenerEvent event) {
        event.addListener(SpawnConditionLoader.INSTANCE);
    }

    public static SpawnCheckResult allowSpawning(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos,
                                                 EntityType<?> entityTypeIn) {
        boolean matchForce = forceSet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        if (matchForce) return SpawnCheckResult.FORCE;
        boolean matchDefault = defaultSet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        if (matchDefault) return SpawnCheckResult.DEFAULT;
        boolean matchDeny = denySet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        if (matchDeny) return SpawnCheckResult.DENY;
        else return SpawnCheckResult.DEFAULT;
    }

    public enum SpawnCheckResult {
        DENY, DEFAULT, FORCE
    }
}
