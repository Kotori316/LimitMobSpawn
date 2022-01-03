package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public record LightLevelLimit(LightLayer layer, int level) implements TestSpawn {
    public static final TestSpawn.Serializer<LightLevelLimit> SERIALIZER = new Serializer();

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        if (worldIn instanceof BlockAndTintGetter getter) {
            int actualLevel = getter.getBrightness(layer, pos);
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

    private static class Serializer extends TestSpawn.Serializer<LightLevelLimit> {

        @Override
        public String getType() {
            return "light";
        }

        @Override
        public <T> LightLevelLimit from(Dynamic<T> dynamic) {
            LightLayer layer = dynamic.get("layer")
                .asString().map(LightLayer::valueOf)
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
            return Set.of("layer", "level");
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable SharedSuggestionProvider provider) {
            return switch (property) {
                case "layer" -> Arrays.stream(LightLayer.values()).map(LightLayer::toString).collect(Collectors.toSet());
                case "level" -> IntStream.rangeClosed(0, 14).mapToObj(Integer::toString).collect(Collectors.toSet());
                default -> Set.of();
            };
        }
    }
}
