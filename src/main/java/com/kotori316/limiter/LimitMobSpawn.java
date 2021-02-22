package com.kotori316.limiter;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityLimit;

@Mod(LimitMobSpawn.MOD_ID)
public class LimitMobSpawn {
    public static final String MOD_ID = "limit-mob-spawn";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static Set<TestSpawn> denySet = new HashSet<>();
    public static Set<TestSpawn> defaultSet = new HashSet<>();

    public LimitMobSpawn() {
        MinecraftForge.EVENT_BUS.register(this);
        TestSpawn nether = new DimensionLimit(World.THE_NETHER);
        TestSpawn pigman = new EntityLimit(EntityType.PIGLIN);
        denySet.add(new DimensionLimit(World.OVERWORLD));
        denySet.add(nether);
        denySet.add(new DimensionLimit(World.THE_END));
        denySet.add(new EntityLimit(EntityType.BAT));
        defaultSet.add(nether.and(pigman));
    }

    @SubscribeEvent
    public void addLister(AddReloadListenerEvent event) {
        event.addListener(SpawnConditionLoader.INSTANCE);
    }

    public static boolean allowSpawning(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos,
                                        EntityType<?> entityTypeIn) {
        boolean matchDefault = defaultSet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        if (matchDefault) return true;
        boolean matchDeny = denySet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        return !matchDeny;
    }
}
