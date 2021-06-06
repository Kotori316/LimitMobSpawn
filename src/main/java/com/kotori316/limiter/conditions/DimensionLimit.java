package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class DimensionLimit implements TestSpawn {
    public static final TestSpawn.Serializer<DimensionLimit> SERIALIZER = new DimensionSerializer();

    private final RegistryKey<World> type;

    public DimensionLimit(RegistryKey<World> type) {
        this.type = type;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", type);
    }

    public static DimensionLimit fromName(String name) {
        return new DimensionLimit(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(name)));
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        RegistryKey<World> type;
        if (worldIn instanceof World) {
            World world = (World) worldIn;
            type = world.getDimensionKey();
        } else {
            type = World.OVERWORLD;
        }
        return this.type == type;
    }

    public RegistryKey<World> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.DimensionLimit that = (com.kotori316.limiter.conditions.DimensionLimit) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "DimensionLimit{" + "type=" + type + '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "dim " + type.getLocation();
    }

    private static final class DimensionSerializer extends StringLimitSerializer<DimensionLimit, RegistryKey<World>> {
        @Override
        public String getType() {
            return "dimension";
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable ISuggestionProvider provider) {
            if (provider == null || !property.equals(saveKey()))
                return Collections.emptySet();
            return provider.func_241861_q()
                .func_230520_a_()
                .keySet()
                .stream()
                .map(ResourceLocation::toString)
                .collect(Collectors.toSet());
        }

        @Override
        public RegistryKey<World> fromString(String s) {
            return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(s));
        }

        @Override
        public String valueToString(RegistryKey<World> worldRegistryKey) {
            return worldRegistryKey.getLocation().toString();
        }

        @Override
        public String saveKey() {
            return "dim";
        }

        @Override
        public DimensionLimit instance(RegistryKey<World> worldRegistryKey) {
            return new DimensionLimit(worldRegistryKey);
        }

        @Override
        public RegistryKey<World> getter(DimensionLimit dimensionLimit) {
            return dimensionLimit.getType();
        }
    }
}
