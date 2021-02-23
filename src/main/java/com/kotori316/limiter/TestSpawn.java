package com.kotori316.limiter;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;

public interface TestSpawn {
    Marker MARKER = MarkerManager.getMarker("TestSpawn");

    boolean test(IBlockReader worldIn,
                 BlockPos pos,
                 EntityType<?> entityTypeIn,
                 @Nullable SpawnReason reason);

    default Serializer<? extends TestSpawn> getSerializer() {
        return EMPTY_SERIALIZER;
    }

    default TestSpawn and(TestSpawn other) {
        return new And(this, other);
    }

    default TestSpawn or(TestSpawn other) {
        return new Or(this, other);
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
        public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
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
