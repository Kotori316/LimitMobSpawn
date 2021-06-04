package com.kotori316.limiter.conditions;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiomeLimitTest extends BeforeAllTest {
    static final BiomeLimit NOT_EXIST_BIOME = new BiomeLimit(new ResourceLocation("lms:not_exist_biome"));

    static Stream<Object[]> registeredBiomes() {
        return ForgeRegistries.BIOMES.getEntries().stream()
            .map(e -> new Object[]{e.getKey(), e.getValue()});
    }

    @Test
    void dummy() {
        assertTrue(registeredBiomes().count() > 0);
    }

    @ParameterizedTest
    @MethodSource("registeredBiomes")
    void testValid(RegistryKey<Biome> key, Biome biome) {
        BiomeLimit limit = new BiomeLimit(key);
        assertAll(
            () -> assertTrue(limit.test(biome)),
            () -> assertFalse(NOT_EXIST_BIOME.test(biome))
        );
    }

    @ParameterizedTest
    @MethodSource("registeredBiomes")
    void cycle(RegistryKey<Biome> key, Biome biome) {
        BiomeLimit limit = new BiomeLimit(key);
        testCycle(limit);
        assertEquals(limit, new BiomeLimit(Objects.requireNonNull(biome.getRegistryName())));
    }
}
