package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DimensionLimitTest extends BeforeAllTest {
    static Stream<ResourceKey<Level>> registryKeys() {
        return Stream.of(
            Level.OVERWORLD,
            Level.NETHER,
            Level.END,
            ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("mining_dimension:mining")),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("kick:gaia_dimension45"))
        );
    }

    @Test
    void dummy() {
        assertTrue(registryKeys().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("registryKeys")
    void serialize(ResourceKey<Level> worldResourceKey) {
        DimensionLimit limit = new DimensionLimit(worldResourceKey);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NbtOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @MethodSource("registryKeys")
    void cycleConsistency(ResourceKey<Level> worldResourceKey) {
        DimensionLimit limit = new DimensionLimit(worldResourceKey);
        testCycle(limit);
    }
}
