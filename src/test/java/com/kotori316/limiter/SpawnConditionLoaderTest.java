package com.kotori316.limiter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Creator;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityClassificationLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.LightLevelLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.RandomLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SpawnConditionLoaderTest extends BeforeAllTest {
    @Test
    void dummy() throws Exception {
        assertTrue(RegisterTest.serializers().length > 0);
        assertTrue(LoadJsonTest.emptyElements().length > 0);
        assertTrue(LoadJsonTest.stupids().length > 0);
        assertTrue(FromJsonFileTest.loadAll().findAny().isPresent());
    }

    static class RegisterTest extends BeforeAllTest {
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

                @Override
                public Set<String> propertyKeys() {
                    return Collections.emptySet();
                }

                @Override
                public Set<String> possibleValues(String property, boolean suggesting, ISuggestionProvider provider) {
                    return Collections.emptySet();
                }
            };
            assertDoesNotThrow(() -> loader.register(serializer));
        }
    }

    static class LoadJsonTest extends BeforeAllTest {
        @Test
        @DisplayName("Load from strange json map")
        void loadFromJsonObject() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.add("a", All.getInstance().toJson());

            Set<TestSpawn> values = loader.getValues(object);
            assertEquals(Collections.singleton(All.getInstance()), values);
        }

        @Test
        @DisplayName("Load from just 1 element.")
        void loadFromJsonObject2() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = All.getInstance().toJson();

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

        @Test
        void loadJsonWitchOnly0() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
            object.add("default", as(
                Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH)
            ));
            object.add("deny", as(
                new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD))
            ));

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH)),
                    loader.getValues(object.get("default"))),
                () -> assertEquals(Collections.singleton(new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD))),
                    loader.getValues(object.get("deny")))
            );
        }

        @Test
        void loadJsonWitchOnly1() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
            object.add("default", Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH).toJson());
            object.add("deny", new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD)).toJson());

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH)),
                    loader.getValues(object.get("default"))),
                () -> assertEquals(Collections.singleton(new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD))),
                    loader.getValues(object.get("deny")))
            );
        }

        @Test
        void loadJsonWitchOnly2() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
            object.add("default", as(
                Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH)
            ));
            object.add("deny", as(
                new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD))
            ));
            Map<ResourceLocation, JsonElement> map = new HashMap<>();
            map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "witch_only"), object);
            loader.apply(map, IResourceManager.Instance.INSTANCE, EmptyProfiler.INSTANCE);

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(World.OVERWORLD, EntityType.WITCH)),
                    loader.getHolder().getDefaultConditions()),
                () -> assertEquals(Collections.singleton(new EntityClassificationLimit(EntityClassification.MONSTER).and(new DimensionLimit(World.OVERWORLD))),
                    loader.getHolder().getDenyConditions())
            );
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

        private static JsonArray as(TestSpawn... conditions) {
            JsonArray array = new JsonArray();
            for (TestSpawn spawn : conditions) {
                array.add(spawn.toJson());
            }
            return array;
        }

    }

    static class FromJsonFileTest extends BeforeAllTest {

        /**
         * @param name resource name without extension.
         */
        SpawnConditionLoader loadFile(String name) {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Gson gson = new Gson();
            try (InputStream stream = getClass().getResourceAsStream(String.format("/data/%s/%s/%s.json", LimitMobSpawn.MOD_ID, LimitMobSpawn.MOD_ID, name));
                 Reader reader = new InputStreamReader(Objects.requireNonNull(stream, String.format("Stream of %s is NULL!", name)))) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                Map<ResourceLocation, JsonElement> map = new HashMap<>();
                map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, name), object);
                loader.apply(map, IResourceManager.Instance.INSTANCE, EmptyProfiler.INSTANCE);
                return loader;
            } catch (IOException e) {
                return fail(e);
            }
        }

        @Test
        @DisplayName("From Json, Bat")
        void loadFromFile1() {
            SpawnConditionLoader loader = loadFile("no_bats");

            assertEquals(Collections.singleton(new EntityLimit(EntityType.BAT).and(new SpawnReasonLimit(SpawnReason.SPAWN_EGG).not())),
                loader.getHolder().getDenyConditions());
            assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
            assertTrue(loader.getHolder().getForceConditions().isEmpty());
        }

        @Test
        @DisplayName("From Json, Peaceful")
        void loadFromFile2() {
            SpawnConditionLoader loader = loadFile("peaceful");

            assertEquals(Collections.singleton(new EntityClassificationLimit(EntityClassification.MONSTER)),
                loader.getHolder().getDenyConditions());
            assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
            assertTrue(loader.getHolder().getForceConditions().isEmpty());
        }

        @Test
        @DisplayName("From Json, Test1")
        void loadFromFile3() {
            SpawnConditionLoader loader = loadFile("test1");

            assertEquals(Sets.newHashSet(
                    Creator.entityAtDimension(World.THE_NETHER, EntityType.PIGLIN),
                    new EntityClassificationLimit(EntityClassification.CREATURE).or(new EntityClassificationLimit(EntityClassification.MISC)),
                    Creator.posAtDimension(World.THE_END, -500, 500, -500, 500)),
                loader.getHolder().getDefaultConditions());
            assertEquals(Sets.newHashSet(
                    new DimensionLimit(World.OVERWORLD),
                    new DimensionLimit(World.THE_NETHER),
                    new DimensionLimit(World.THE_END),
                    new EntityLimit(EntityType.BAT)),
                loader.getHolder().getDenyConditions());
            assertTrue(loader.getHolder().getForceConditions().isEmpty());
        }

        @Test
        @DisplayName("Random Limit")
        void loadRandomLimit() {
            SpawnConditionLoader loader = loadFile("cancel_70");

            assertEquals(Collections.singleton(
                    new And(
                        new RandomLimit(0.7),
                        new Or(new SpawnReasonLimit(SpawnReason.NATURAL), new SpawnReasonLimit(SpawnReason.REINFORCEMENT))
                    )
                ),
                loader.getHolder().getDenyConditions());
            assertTrue(loader.getHolder().getForceConditions().isEmpty());
            assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
        }

        @Test
        @DisplayName("From Json, Enderman only")
        void loadFromFile4() {
            SpawnConditionLoader loader = loadFile("enderman_only");

            assertEquals(
                Collections.singleton(new EntityLimit(EntityType.ENDERMAN)),
                loader.getHolder().getForceConditions()
            );
            assertEquals(
                Collections.singleton(All.getInstance()),
                loader.getHolder().getDenyConditions()
            );
            assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
        }

        @Test
        @DisplayName("From Json, Light")
        void loadFromFile5() {
            SpawnConditionLoader loader = loadFile("allow_only_0");

            assertEquals(
                Collections.singleton(new And(
                    new EntityClassificationLimit(EntityClassification.MONSTER),
                    new LightLevelLimit(LightType.BLOCK, 0)
                )),
                loader.getHolder().getDenyConditions()
            );
        }

        @ParameterizedTest
        @MethodSource
        void loadAll(String name) {
            URL url = getClass().getResource(String.format("/data/%s/%s/%s.json", LimitMobSpawn.MOD_ID, LimitMobSpawn.MOD_ID, name));
            assertNotNull(url, String.format("File %s.json doesn't exist.", name));
        }

        static Stream<String> loadAll() throws Exception {
            Class<?> aClass = Class.forName("com.kotori316.limiter.data.Rules");
            return Stream.of(aClass.getDeclaredMethods())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .filter(m -> m.getParameterTypes().length == 0)
                .filter(m -> m.getReturnType() == JsonObject.class)
                .map(Method::getName);
        }
    }

    static class LoadAndTest extends BeforeAllTest {
        static JsonObject makeObject(String type, JsonElement value) {
            JsonObject condition = new JsonObject();
            condition.addProperty("type", type);
            condition.add("values", value);
            return condition;
        }

        @Test
        void contains1And() {
            JsonArray value = new JsonArray();
            value.add(All.getInstance().toJson());
            JsonObject andJson = makeObject("and", value);

            TestSpawn and = SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(JsonOps.INSTANCE, andJson));
            assertEquals(new And(All.getInstance()), and);
        }

        @Test
        void contains1Or() {
            JsonArray value = new JsonArray();
            value.add(All.getInstance().toJson());
            JsonObject orJson = makeObject("or", value);

            TestSpawn or = Or.SERIALIZER.from(new Dynamic<>(JsonOps.INSTANCE, orJson));
            assertEquals(new Or(All.getInstance()), or);
        }
    }
}
