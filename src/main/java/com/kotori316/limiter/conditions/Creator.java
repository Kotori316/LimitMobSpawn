package com.kotori316.limiter.conditions;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import com.kotori316.limiter.TestSpawn;

public class Creator {
    public static TestSpawn posAtDimension(ResourceKey<Level> dimension, BlockPos pos1, BlockPos pos2) {
        return new PositionLimit(pos1, pos2).and(new DimensionLimit(dimension));
    }

    public static TestSpawn posAtDimension(ResourceKey<Level> dimension, int minX, int maxX, int minZ, int maxZ) {
        return new PositionLimit(minX, maxX, 0, 256, minZ, maxZ).and(new DimensionLimit(dimension));
    }

    public static TestSpawn entityAtDimension(ResourceKey<Level> dimension, EntityType<?> entityType) {
        return new EntityLimit(entityType).and(new DimensionLimit(dimension));
    }
}
