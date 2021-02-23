package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class And implements TestSpawn {
    public static final TestSpawn.Serializer<And> SERIALIZER = new Serializer();
    private final TestSpawn t1, t2;

    public And(TestSpawn t1, TestSpawn t2) {
        this.t1 = t1;
        this.t2 = t2;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}, {}", t1, t2);
    }

    @Override
    public boolean test(IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return t1.test(worldIn, pos, entityTypeIn, reason) && t2.test(worldIn, pos, entityTypeIn, reason);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.And and = (com.kotori316.limiter.conditions.And) o;
        return Objects.equals(t1, and.t1) && Objects.equals(t2, and.t2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, t2);
    }

    @Override
    public String toString() {
        return "And{" +
            "t1=" + t1 +
            ", t2=" + t2 +
            '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<And> {

        @Override
        public String getType() {
            return "and";
        }

        @Override
        public And fromJson(JsonObject object) {
            TestSpawn t1 = SpawnConditionLoader.INSTANCE.deserialize(object.getAsJsonObject("t1"));
            TestSpawn t2 = SpawnConditionLoader.INSTANCE.deserialize(object.getAsJsonObject("t2"));
            return new And(t1, t2);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            And and = (And) t;
            JsonObject object = new JsonObject();
            object.add("t1", and.t1.toJson());
            object.add("t2", and.t2.toJson());
            return object;
        }
    }
}
