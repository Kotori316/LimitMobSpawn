package com.kotori316.limiter;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

@Mod("limit-mob-spawn")
public class LimitMobSpawn {
    private static final Set<TestSpawn> denySet = new HashSet<>();
    private static final Set<TestSpawn> defaultSet = new HashSet<>();

    public LimitMobSpawn() {
        MinecraftForge.EVENT_BUS.register(this);
        TestSpawn nether = new TestSpawn.DimensionLimit(World.THE_NETHER);
        TestSpawn pigman = new TestSpawn.EntityLimit(EntityType.PIGLIN);
        denySet.add(new TestSpawn.DimensionLimit(World.OVERWORLD));
        denySet.add(nether);
        denySet.add(new TestSpawn.DimensionLimit(World.THE_END));
        denySet.add(new TestSpawn.EntityLimit(EntityType.BAT));
        defaultSet.add(nether.and(pigman));
    }

    public static boolean allowSpawning(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos,
                                        EntityType<?> entityTypeIn) {
        boolean matchDefault = defaultSet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        if (matchDefault) return true;
        boolean matchDeny = denySet.stream().anyMatch(spawn -> spawn.test(placeType, worldIn, pos, entityTypeIn));
        return !matchDeny;
    }
}
