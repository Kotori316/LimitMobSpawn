package com.kotori316.limiter.conditions;

import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.ForgeRegistries;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.LimitMobSpawn;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("The biome is now registered dynamically so we can't use in unit test.")
class BiomeLimitTest extends BeforeAllTest {
    static final BiomeLimit NOT_EXIST_BIOME = new BiomeLimit(new ResourceLocation("lms:not_exist_biome"));

    static Stream<Arguments> registeredBiomes() {
        var key = ResourceKey.create(ForgeRegistries.Keys.BIOMES, new ResourceLocation(LimitMobSpawn.MOD_ID, "test_biome"));
        var biome = new Biome.BiomeBuilder()
            .precipitation(Biome.Precipitation.NONE)
            .temperature(0.5F)
            .downfall(0.5F)
            .specialEffects(new BiomeSpecialEffects.Builder()
                .waterColor(0).waterFogColor(0).fogColor(0).skyColor(0)
                .build())
            .mobSpawnSettings(new MobSpawnSettings.Builder().build())
            .generationSettings(BiomeGenerationSettings.EMPTY)
            .temperatureAdjustment(Biome.TemperatureModifier.NONE)
            .build();

        return Stream.of(Arguments.of(key, biome));
    }

    @Test
    void dummy() {
        assertTrue(registeredBiomes().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("registeredBiomes")
    void testValid(ResourceKey<Biome> key, Biome biome) {
        BiomeLimit limit = new BiomeLimit(key);
        assertAll(
            () -> assertTrue(limit.test(biome)),
            () -> assertFalse(NOT_EXIST_BIOME.test(biome))
        );
    }

    @ParameterizedTest
    @MethodSource("registeredBiomes")
    void cycle(ResourceKey<Biome> key, Biome biome) {
        BiomeLimit limit = new BiomeLimit(key);
        testCycle(limit);
        assertEquals(limit, new BiomeLimit(Objects.requireNonNull(ForgeRegistries.BIOMES.getKey(biome))));
    }
}
