package com.kotori316.limiter.conditions;

import java.util.Locale;
import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class EntityClassificationLimit implements TestSpawn {
    public static final TestSpawn.Serializer<EntityClassificationLimit> SERIALIZER = new Serializer();
    private final EntityClassification classification;

    public EntityClassificationLimit(EntityClassification classification) {
        this.classification = classification;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", classification);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return entityTypeIn.getClassification() == this.classification;
    }

    @Override
    public String toString() {
        return "EntityClassificationLimit{" +
            "classification=" + classification +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityClassificationLimit that = (EntityClassificationLimit) o;
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

    private static class Serializer extends TestSpawn.Serializer<EntityClassificationLimit> {
        @Override
        public String getType() {
            return "classification";
        }

        @Override
        public EntityClassificationLimit fromJson(JsonObject object) {
            EntityClassification classification = EntityClassification.getClassificationByName(
                JSONUtils.getString(object, "classification").toLowerCase(Locale.ROOT));
            return new EntityClassificationLimit(classification);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            EntityClassificationLimit l = (EntityClassificationLimit) t;
            JsonObject object = new JsonObject();
            object.addProperty("classification", l.classification.getName());
            return object;
        }
    }
}
