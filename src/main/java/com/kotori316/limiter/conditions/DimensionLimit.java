package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public record DimensionLimit(ResourceKey<Level> type) implements TestSpawn {
    public static final TestSpawn.Serializer<DimensionLimit> SERIALIZER = new DimensionSerializer();

    public DimensionLimit(ResourceKey<Level> type) {
        this.type = type;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", type);
    }

    public static DimensionLimit fromName(String name) {
        return new DimensionLimit(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(name)));
    }

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, MobSpawnType reason) {
        ResourceKey<Level> type;
        if (worldIn instanceof Level level) {
            type = level.dimension();
        } else {
            type = Level.OVERWORLD;
        }
        return this.type == type;
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "dim " + type.location();
    }

    private static final class DimensionSerializer extends StringLimitSerializer<DimensionLimit, ResourceKey<Level>> {
        @Override
        public String getType() {
            return "dimension";
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable SharedSuggestionProvider provider) {
            if (provider == null || !property.equals(saveKey()))
                return Collections.emptySet();
            return provider.levels()
                .stream()
                .map(ResourceKey::location)
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
        }

        @Override
        public ResourceKey<Level> fromString(String s) {
            return ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(s));
        }

        @Override
        public String valueToString(ResourceKey<Level> worldResourceKey) {
            return worldResourceKey.location().toString();
        }

        @Override
        public String saveKey() {
            return "dim";
        }

        @Override
        public DimensionLimit instance(ResourceKey<Level> worldResourceKey) {
            return new DimensionLimit(worldResourceKey);
        }

        @Override
        public ResourceKey<Level> getter(DimensionLimit dimensionLimit) {
            return dimensionLimit.type();
        }
    }
}
