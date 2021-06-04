package com.kotori316.limiter;

import java.util.Collections;
import java.util.Set;

import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
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

    Serializer<? extends TestSpawn> getSerializer();

    String contentShort();

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
        JsonObject object = getSerializer().to(this, JsonOps.INSTANCE).getAsJsonObject();
        object.addProperty("type", getSerializer().getType());
        return object;
    }

    default <T> T to(DynamicOps<T> ops) {
        T map = getSerializer().to(this, ops);
        return ops.mergeToMap(map, ops.createString("type"), ops.createString(getSerializer().getType()))
            .getOrThrow(true, s -> LimitMobSpawn.LOGGER.error("Error in serialize {}, {}", this, s));
    }

    abstract class Serializer<A extends TestSpawn> {
        public abstract String getType();

        public abstract <T> A from(Dynamic<T> dynamic);

        public abstract <T> T to(TestSpawn a, DynamicOps<T> ops);

        public abstract Set<String> propertyKeys();

        public abstract Set<String> possibleValues(String property, boolean suggesting, @Nullable ISuggestionProvider provider);
    }

    enum Empty implements TestSpawn {
        INSTANCE;

        @Override
        public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
            return false;
        }

        @Override
        public Serializer<? extends TestSpawn> getSerializer() {
            return EMPTY_SERIALIZER;
        }

        @Override
        public String contentShort() {
            return getSerializer().getType();
        }
    }

    Serializer<Empty> EMPTY_SERIALIZER = new Serializer<Empty>() {
        @Override
        public String getType() {
            return "anonymous";
        }

        @Override
        public <T> Empty from(Dynamic<T> dynamic) {
            return Empty.INSTANCE;
        }

        @Override
        public <T> T to(TestSpawn a, DynamicOps<T> ops) {
            return ops.emptyMap();
        }

        @Override
        public Set<String> propertyKeys() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, ISuggestionProvider provider) {
            return Collections.emptySet();
        }
    };
}
