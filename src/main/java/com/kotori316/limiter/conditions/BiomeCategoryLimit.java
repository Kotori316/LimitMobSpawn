package com.kotori316.limiter.conditions;

import java.util.Objects;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class BiomeCategoryLimit implements TestSpawn {
    public static final TestSpawn.Serializer<BiomeCategoryLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        BiomeCategoryLimit::getCategory, BiomeCategoryLimit::new, Biome.BiomeCategory::getName,
        Biome.BiomeCategory::byName, "category", "category",
        Biome.BiomeCategory.values());
    private final Biome.BiomeCategory category;

    public BiomeCategoryLimit(Biome.BiomeCategory category) {
        this.category = category;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", category);
    }

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        if (worldIn instanceof LevelReader worldReader) {
            Biome biome = worldReader.getBiome(pos);
            return category == biome.getBiomeCategory();
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BiomeCategoryLimit that = (BiomeCategoryLimit) o;
        return category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(category);
    }

    @Override
    public String toString() {
        return "BiomeCategoryLimit{" +
            "category=" + category +
            '}';
    }

    @Override
    public Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "category " + category;
    }

    public Biome.BiomeCategory getCategory() {
        return category;
    }
}
