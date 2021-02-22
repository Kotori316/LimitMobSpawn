package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import com.kotori316.limiter.TestSpawn;

public class EntityTypeLimit implements TestSpawn {
    public static final TestSpawn.Serializer<EntityTypeLimit> SERIALIZER = new Serializer();
    private final EntityClassification classification;

    public EntityTypeLimit(EntityClassification classification) {
        this.classification = classification;
    }

    @Override
    public boolean test(IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return entityTypeIn.getClassification() == this.classification;
    }

    @Override
    public String toString() {
        return "EntityTypeLimit{" +
            "classification=" + classification +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityTypeLimit that = (EntityTypeLimit) o;
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

    private static class Serializer extends TestSpawn.Serializer<EntityTypeLimit> {
        @Override
        public String getType() {
            return "classification";
        }

        @Override
        public EntityTypeLimit fromJson(JsonObject object) {
            EntityClassification classification = EntityClassification.getClassificationByName(JSONUtils.getString(object, "classification"));
            return new EntityTypeLimit(classification);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            EntityTypeLimit l = (EntityTypeLimit) t;
            JsonObject object = new JsonObject();
            object.addProperty("classification", l.classification.getName());
            return object;
        }
    }
}
