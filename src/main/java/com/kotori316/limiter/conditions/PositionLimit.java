package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public record PositionLimit(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) implements TestSpawn {
    public static final TestSpawn.Serializer<PositionLimit> SERIALIZER = new Serializer();

    public PositionLimit(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with ({},{},{}),({},{},{})", minX, minY, minZ, maxX, maxY, maxZ);
    }

    public PositionLimit(BlockPos pos1, BlockPos pos2) {
        this(
            Math.min(pos1.getX(), pos2.getX()),  // minX
            Math.max(pos1.getX(), pos2.getX()),  // maxX
            Math.min(pos1.getY(), pos2.getY()),  // minY
            Math.max(pos1.getY(), pos2.getY()),  // maxY
            Math.min(pos1.getZ(), pos2.getZ()),  // minZ
            Math.max(pos1.getZ(), pos2.getZ())   // maxZ
        );
    }

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        return minX <= pos.getX() && pos.getX() < maxX &&
            minY <= pos.getY() && pos.getY() < maxY &&
            minZ <= pos.getZ() && pos.getZ() < maxZ;
    }

    @Override
    public String toString() {
        return "PositionLimit{" +
            '(' + minX + ", " + minY + ", " + minZ + ") -> (" + maxX + ", " + maxY + ", " + maxZ + ')' +
            '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "(" + minX + ", " + minY + ", " + minZ + ") -> (" + maxX + ", " + maxY + ", " + maxZ + ")";
    }

    private static class Serializer extends TestSpawn.Serializer<PositionLimit> {
        @Override
        public String getType() {
            return "position";
        }

        @Override
        public <T> PositionLimit from(Dynamic<T> dynamic) {
            Map<String, Dynamic<T>> map = dynamic.asMap(d -> d.asString(""), Function.identity());
            if (map.containsKey("pos1") && map.containsKey("pos2")) {
                return BlockPos.CODEC.decode(map.get("pos1")).flatMap(pos1 ->
                        BlockPos.CODEC.decode(map.get("pos2")).map(pos2 ->
                            new PositionLimit(pos1.getFirst(), pos2.getFirst())))
                    .getOrThrow(true, s -> LimitMobSpawn.LOGGER.error("Erred when loading PositionLimit, {}", s));
            } else {
                // Map of minX, maxX, etc.
                int minX = map.get("minX").asInt(0);
                int maxX = map.get("maxX").asInt(0);
                int minY = map.getOrDefault("minY", dynamic.createInt(0)).asInt(0);
                int maxY = map.getOrDefault("maxY", dynamic.createInt(256)).asInt(256);
                int minZ = map.get("minZ").asInt(0);
                int maxZ = map.get("maxZ").asInt(0);
                return new PositionLimit(minX, maxX, minY, maxY, minZ, maxZ);
            }
        }

        @Override
        public <T> T to(TestSpawn t, DynamicOps<T> ops) {
            PositionLimit l = (PositionLimit) t;
            Map<T, T> map = new HashMap<>();
            BlockPos.CODEC.encodeStart(ops, new BlockPos(l.minX, l.minY, l.minZ)).result().ifPresent(
                pos1 -> map.put(ops.createString("pos1"), pos1)
            );
            BlockPos.CODEC.encodeStart(ops, new BlockPos(l.maxX, l.maxY, l.maxZ)).result().ifPresent(
                pos2 -> map.put(ops.createString("pos2"), pos2)
            );
            return ops.createMap(map);
        }

        @Override
        public Set<String> propertyKeys() {
            return Sets.newHashSet("minX", "maxX", "minY", "maxY", "minZ", "maxZ");
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, SharedSuggestionProvider provider) {
            return Collections.emptySet();
        }
    }
}
