package com.kotori316.limiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;

public interface TestSpawn {
    Marker MARKER = MarkerManager.getMarker("TestSpawn");

    boolean test(BlockGetter worldIn,
                 BlockPos pos,
                 EntityType<?> entityTypeIn,
                 @Nullable MobSpawnType reason);

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

    default boolean isDeterministic() {
        return true;
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

        public abstract Set<String> possibleValues(String property, boolean suggesting, @Nullable SharedSuggestionProvider provider);

        public Set<ResourceLocation> suggestions(String property, @Nullable SharedSuggestionProvider provider) {
            return Collections.emptySet();
        }

        protected <T> Map<T, T> writeMap(DynamicOps<T> ops, Stream<TestSpawn> tsStream) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("values"), ops.createList(tsStream.map(ts -> ts.to(ops))));
            return map;
        }

        protected <T, S> S getCombinationFrom(Dynamic<T> dynamic, Function<List<TestSpawn>, S> listFunction) {
            return dynamic.get("values").map(d -> d.asList(SpawnConditionLoader.INSTANCE::deserialize)).result()
                .filter(Predicate.not(List::isEmpty))
                .map(listFunction)
                .orElseThrow(() -> new IllegalStateException(getType() + " object has no child conditions. " + dynamic.getValue()));
        }
    }

    enum Empty implements TestSpawn {
        INSTANCE;

        @Override
        public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, MobSpawnType reason) {
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

    Serializer<Empty> EMPTY_SERIALIZER = new Serializer<>() {
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
        public Set<String> possibleValues(String property, boolean suggesting, SharedSuggestionProvider provider) {
            return Collections.emptySet();
        }
    };
}
