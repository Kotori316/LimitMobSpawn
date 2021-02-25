package com.kotori316.limiter.conditions;

import java.util.Objects;

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
    public static final TestSpawn.Serializer<DimensionLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        DimensionLimit::getType, DimensionLimit::new,
        key -> key.getLocation().toString(), s -> RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(s)),
        "dim", "dimension"
    );

    private final RegistryKey<World> type;

    public DimensionLimit(RegistryKey<World> type) {
        this.type = type;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", type);
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

}
