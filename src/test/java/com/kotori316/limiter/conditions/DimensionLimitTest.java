package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void dummy() {
        assertTrue(registryKeys().findAny().isPresent());
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
        testCycle(limit);
    }
}
