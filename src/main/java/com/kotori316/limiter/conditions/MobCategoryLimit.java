package com.kotori316.limiter.conditions;

import java.util.Locale;
import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class MobCategoryLimit implements TestSpawn {
    public static final TestSpawn.Serializer<MobCategoryLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        MobCategoryLimit::getClassification, MobCategoryLimit::new, MobCategory::getName,
        s -> MobCategory.byName(s.toLowerCase(Locale.ROOT)), "classification", "classification",
        MobCategory.values()
    );
    private final MobCategory classification;

    public MobCategoryLimit(MobCategory classification) {
        this.classification = classification;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", classification);
    }

    public MobCategory getClassification() {
        return classification;
    }

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, MobSpawnType reason) {
        return entityTypeIn.getCategory() == this.classification;
    }

    @Override
    public String toString() {
        return "MobCategoryLimit{" +
            "classification=" + classification +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MobCategoryLimit that = (MobCategoryLimit) o;
        return classification == that.classification;
    }

    @Override
    public int hashCode() {
        return Objects.hash(classification);
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "classification=" + classification.name();
    }

}
