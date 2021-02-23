package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class Not implements TestSpawn {
    public static final TestSpawn.Serializer<Not> SERIALIZER = new Serializer();
    private final TestSpawn value;

    public Not(TestSpawn value) {
        this.value = value;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.Not not = (com.kotori316.limiter.conditions.Not) o;
        return Objects.equals(value, not.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Not{" +
            "value=" + value +
            '}';
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return !value.test(worldIn, pos, entityTypeIn, reason);
    }

    @Override
    public TestSpawn not() {
        return value;
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<Not> {

        @Override
        public String getType() {
            return "not";
        }

        @Override
        public Not fromJson(JsonObject object) {
            TestSpawn t1 = SpawnConditionLoader.INSTANCE.deserialize(object.getAsJsonObject("value"));
            return new Not(t1);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            Not not = (Not) t;
            JsonObject object = new JsonObject();
            object.add("value", not.value.toJson());
            return object;
        }
    }
}
