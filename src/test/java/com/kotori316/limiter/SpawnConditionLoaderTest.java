package com.kotori316.limiter;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityClassificationLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpawnConditionLoaderTest extends BeforeAllTest {

    static class Register {
        static TestSpawn.Serializer<?>[] serializers() {
            return new TestSpawn.Serializer[]{
                TestSpawn.EMPTY_SERIALIZER,
                All.SERIALIZER,
                And.SERIALIZER,
                Or.SERIALIZER,
                Not.SERIALIZER,
                DimensionLimit.SERIALIZER,
                EntityLimit.SERIALIZER,
                EntityClassificationLimit.SERIALIZER,
                PositionLimit.SERIALIZER,
                SpawnReasonLimit.SERIALIZER
            };
        }

        @ParameterizedTest
        @MethodSource("serializers")
        void putErrorTest(TestSpawn.Serializer<?> serializer) {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            assertThrows(IllegalArgumentException.class,
                () -> loader.register(serializer));
        }

        @Test
        void outNormal() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            TestSpawn.Serializer<?> serializer = new TestSpawn.Serializer<TestSpawn>() {
                @Override
                public String getType() {
                    return "newType";
                }

                @Override
                public <T> TestSpawn from(Dynamic<T> dynamic) {
                    return null;
                }

                @Override
                public <T> T to(TestSpawn a, DynamicOps<T> ops) {
                    return null;
                }
            };
            assertDoesNotThrow(() -> loader.register(serializer));
        }
    }

    static class LoadJson {
        @Test
        void loadFromJsonObject() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.add("a", All.getInstance().toJson());

            Set<TestSpawn> values = loader.getValues(object);
            assertEquals(Collections.singleton(All.getInstance()), values);
        }

        @Test
        void loadFromJsonArray() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonArray a = new JsonArray();
            a.add(All.getInstance().toJson());

            Set<TestSpawn> values = loader.getValues(a);
            assertEquals(Collections.singleton(All.getInstance()), values);
        }

        @Test
        void loadFromJsonArray2() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Set<TestSpawn> ans = Sets.newHashSet(
                new DimensionLimit(World.THE_NETHER),
                new EntityClassificationLimit(EntityClassification.CREATURE),
                new SpawnReasonLimit(SpawnReason.SPAWNER),
                new PositionLimit(new BlockPos(-10, 5, 64), new BlockPos(24, 65, 95)),
                new PositionLimit(-15, 263, 2, 45, 624, 964),
                new EntityLimit("minecraft:zombie"),
                new EntityClassificationLimit(EntityClassification.WATER_CREATURE),
                new DimensionLimit(World.THE_END),
                new SpawnReasonLimit(SpawnReason.CHUNK_GENERATION)
            );

            JsonArray a = new JsonArray();
            ans.stream().map(TestSpawn::toJson).forEach(a::add);
            Set<TestSpawn> read = loader.getValues(a);
            assertEquals(ans, read);
        }

        static JsonElement[] stupids() {
            return new JsonElement[]{
                JsonNull.INSTANCE, new JsonPrimitive("value"), new JsonPrimitive(false), null
            };
        }

        @ParameterizedTest
        @MethodSource("stupids")
        void stupidInputs(JsonElement element) {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();

            Set<TestSpawn> values = loader.getValues(element);
            assertTrue(values.isEmpty());
        }

        static JsonElement[] emptyElements() {
            return new JsonElement[]{new JsonArray(), new JsonObject()};
        }

        @ParameterizedTest
        @MethodSource("emptyElements")
        void emptyInput(JsonElement empty) {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();

            Set<TestSpawn> values = loader.getValues(empty);
            assertTrue(values.isEmpty());
        }
    }
}
