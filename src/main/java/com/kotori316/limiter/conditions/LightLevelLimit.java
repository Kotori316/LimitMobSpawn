package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.collect.Sets;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public final class LightLevelLimit implements TestSpawn {
    public static final TestSpawn.Serializer<LightLevelLimit> SERIALIZER = new Serializer();
    private final LightType layer;
    private final int level;

    public LightLevelLimit(LightType layer, int level) {
        this.layer = layer;
        this.level = level;
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        if (worldIn instanceof IBlockDisplayReader) {
            int actualLevel = ((IBlockDisplayReader) worldIn).getLightFor(layer, pos);
            return actualLevel > level;
        } else {
            return false;
        }
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return layer.toString().toLowerCase() + "@" + level;
    }

    @Override
    public String toString() {
        return "LightLevelLimit{" +
            "layer=" + layer +
            ", level=" + level +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightLevelLimit limit = (LightLevelLimit) o;
        return level == limit.level && layer == limit.layer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(layer, level);
    }

    private static class Serializer extends TestSpawn.Serializer<LightLevelLimit> {

        @Override
        public String getType() {
            return "light";
        }

        @Override
        public <T> LightLevelLimit from(Dynamic<T> dynamic) {
            LightType layer = dynamic.get("layer")
                .asString().map(LightType::valueOf)
                .getOrThrow(true, s -> LimitMobSpawn.LOGGER.error("Erred when loading LightLevelLimit, {}", s));
            int level = dynamic.get("level")
                .asNumber()
                .getOrThrow(true, s -> LimitMobSpawn.LOGGER.error("Erred when loading LightLevelLimit, {}", s))
                .intValue();
            return new LightLevelLimit(layer, level);
        }

        @Override
        public <T> T to(TestSpawn a, DynamicOps<T> ops) {
            Map<T, T> map = new HashMap<>();
            LightLevelLimit limit = (LightLevelLimit) a;
            map.put(ops.createString("layer"), ops.createString(limit.layer.name()));
            map.put(ops.createString("level"), ops.createInt(limit.level));

            return ops.createMap(map);
        }

        @Override
        public Set<String> propertyKeys() {
            return Sets.newHashSet("layer", "level");
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable ISuggestionProvider provider) {
            switch (property) {
                case "layer":
                    return Arrays.stream(LightType.values()).map(LightType::toString).collect(Collectors.toSet());
                case "level":
                    return IntStream.rangeClosed(0, 14).mapToObj(Integer::toString).collect(Collectors.toSet());
                default:
                    return Collections.emptySet();
            }
        }
    }
}
