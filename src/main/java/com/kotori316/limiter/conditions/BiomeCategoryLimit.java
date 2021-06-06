package com.kotori316.limiter.conditions;

import java.util.Objects;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class BiomeCategoryLimit implements TestSpawn {
    public static final TestSpawn.Serializer<BiomeCategoryLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        BiomeCategoryLimit::getCategory, BiomeCategoryLimit::new, Biome.Category::getName,
        Biome.Category::byName, "category", "category",
        Biome.Category.values());
    private final Biome.Category category;

    public BiomeCategoryLimit(Biome.Category category) {
        this.category = category;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", category);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        if (worldIn instanceof IWorldReader) {
            IWorldReader worldReader = (IWorldReader) worldIn;
            Biome biome = worldReader.getBiome(pos);
            return category == biome.getCategory();
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

    public Biome.Category getCategory() {
        return category;
    }
}
