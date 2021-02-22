package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class Not implements TestSpawn {
    public static final TestSpawn.Serializer<Not> SERIALIZER = new Serializer();
    private final TestSpawn t1;

    public Not(TestSpawn t1) {
        this.t1 = t1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.Not not = (com.kotori316.limiter.conditions.Not) o;
        return Objects.equals(t1, not.t1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1);
    }

    @Override
    public String toString() {
        return "Not{" +
            "t1=" + t1 +
            '}';
    }

    @Override
    public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
        return !t1.test(placeType, worldIn, pos, entityTypeIn);
    }

    @Override
    public TestSpawn not() {
        return t1;
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
            TestSpawn t1 = SpawnConditionLoader.INSTANCE.deserialize(object.getAsJsonObject("t1"));
            return new Not(t1);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            Not not = (Not) t;
            JsonObject object = new JsonObject();
            object.add("t1", not.t1.toJson());
            return object;
        }
    }
}
