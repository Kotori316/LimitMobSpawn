package com.kotori316.limiter.conditions;

import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kotori316.limiter.TestSpawn;

public class Creator {
    public static TestSpawn posAtDimension(RegistryKey<World> dimension, BlockPos pos1, BlockPos pos2) {
        return new PositionLimit(pos1, pos2).and(new DimensionLimit(dimension));
    }

    public static TestSpawn posAtDimension(RegistryKey<World> dimension, int minX, int maxX, int minZ, int maxZ) {
        return new PositionLimit(minX, maxX, 0, 256, minZ, maxZ).and(new DimensionLimit(dimension));
    }

    public static TestSpawn entityAtDimension(RegistryKey<World> dimension, EntityType<?> entityType) {
        return new EntityLimit(entityType).and(new DimensionLimit(dimension));
    }
}
