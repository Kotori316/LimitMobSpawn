package com.kotori316.limiter.conditions;

import net.minecraft.world.level.biome.Biome;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.BeforeAllTest;

class BiomeCategoryLimitTest extends BeforeAllTest {
    @ParameterizedTest
    @EnumSource(Biome.BiomeCategory.class)
    void cycleConsistency(Biome.BiomeCategory category) {
        testCycle(new BiomeCategoryLimit(category));
    }
}
