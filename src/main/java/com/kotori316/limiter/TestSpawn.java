package com.kotori316.limiter;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Not;

public interface TestSpawn {

    boolean test(EntitySpawnPlacementRegistry.PlacementType placeType,
                 IWorldReader worldIn,
                 BlockPos pos,
                 EntityType<?> entityTypeIn);

    default Serializer<? extends TestSpawn> getSerializer() {
        return EMPTY_SERIALIZER;
    }

    default TestSpawn and(TestSpawn other) {
        return new And(this, other);
    }

    default TestSpawn not() {
        return new Not(this);
    }

    default JsonObject toJson() {
        JsonObject object = getSerializer().toJson(this);
        object.addProperty("type", getSerializer().getType());
        return object;
    }

    abstract class Serializer<A extends TestSpawn> {
        public abstract String getType();

        public abstract A fromJson(JsonObject object);

        public abstract JsonObject toJson(TestSpawn a);
    }

    enum Empty implements TestSpawn {
        INSTANCE;

        @Override
        public boolean test(EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn) {
            return false;
        }
    }

    Serializer<Empty> EMPTY_SERIALIZER = new Serializer<Empty>() {
        @Override
        public String getType() {
            return "anonymous";
        }

        @Override
        public Empty fromJson(JsonObject object) {
            return Empty.INSTANCE;
        }

        @Override
        public JsonObject toJson(TestSpawn empty) {
            return new JsonObject();
        }
    };
}
