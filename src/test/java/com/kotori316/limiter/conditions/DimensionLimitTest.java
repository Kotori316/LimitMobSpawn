package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.SpawnConditionLoader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DimensionLimitTest extends BeforeAllTest {
    static Stream<RegistryKey<World>> registryKeys() {
        return Stream.of(
            World.OVERWORLD,
            World.THE_NETHER,
            World.THE_END,
            RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation("mining_dimension:mining")),
            RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation("kick:gaia_dimension45"))
        );
    }

    @ParameterizedTest
    @MethodSource("registryKeys")
    void serialize(RegistryKey<World> worldRegistryKey) {
        DimensionLimit limit = new DimensionLimit(worldRegistryKey);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NBTDynamicOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @MethodSource("registryKeys")
    void cycleConsistency(RegistryKey<World> worldRegistryKey) {
        DimensionLimit limit = new DimensionLimit(worldRegistryKey);
        assertAll(
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(JsonOps.INSTANCE, limit.to(JsonOps.INSTANCE)))),
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(JsonOps.COMPRESSED, limit.to(JsonOps.COMPRESSED)))),
            () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(NBTDynamicOps.INSTANCE, limit.to(NBTDynamicOps.INSTANCE))))
        );
    }
}
