package com.kotori316.limiter.conditions;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public record BiomeCategoryLimit(Biome.BiomeCategory category) implements TestSpawn {
    public static final TestSpawn.Serializer<BiomeCategoryLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        BiomeCategoryLimit::getCategory, BiomeCategoryLimit::new, Biome.BiomeCategory::getName,
        Biome.BiomeCategory::byName, "category", "category",
        Biome.BiomeCategory.values());

    public BiomeCategoryLimit(Biome.BiomeCategory category) {
        this.category = category;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", category);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        if (worldIn instanceof LevelReader worldReader) {
            var biome = worldReader.getBiome(pos);
            return category == Biome.getBiomeCategory(biome);
        }
        return false;
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
