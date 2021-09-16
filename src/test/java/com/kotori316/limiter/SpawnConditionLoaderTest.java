package com.kotori316.limiter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Creator;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.MobCategoryLimit;
import com.kotori316.limiter.conditions.MobSpawnTypeLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.RandomLimit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SpawnConditionLoaderTest extends BeforeAllTest {
    @Test
    void dummy() {
        assertTrue(Register.serializers().length > 0);
        assertTrue(LoadJson.emptyElements().length > 0);
        assertTrue(LoadJson.stupids().length > 0);
    }

    static class Register extends BeforeAllTest {
        static TestSpawn.Serializer<?>[] serializers() {
            return new TestSpawn.Serializer[]{
                TestSpawn.EMPTY_SERIALIZER,
                All.SERIALIZER,
                And.SERIALIZER,
                Or.SERIALIZER,
                Not.SERIALIZER,
                DimensionLimit.SERIALIZER,
                EntityLimit.SERIALIZER,
                MobCategoryLimit.SERIALIZER,
                PositionLimit.SERIALIZER,
                MobSpawnTypeLimit.SERIALIZER
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
                public Set<String> possibleValues(String property, boolean suggesting, SharedSuggestionProvider provider) {
                    return Collections.emptySet();
                }
            };
            assertDoesNotThrow(() -> loader.register(serializer));
        }
    }

    static class LoadJson extends BeforeAllTest {
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
                new DimensionLimit(Level.NETHER),
                new MobCategoryLimit(MobCategory.CREATURE),
                new MobSpawnTypeLimit(MobSpawnType.SPAWNER),
                new PositionLimit(new BlockPos(-10, 5, 64), new BlockPos(24, 65, 95)),
                new PositionLimit(-15, 263, 2, 45, 624, 964),
                new EntityLimit("minecraft:zombie"),
                new MobCategoryLimit(MobCategory.WATER_CREATURE),
                new DimensionLimit(Level.END),
                new MobSpawnTypeLimit(MobSpawnType.CHUNK_GENERATION)
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
                Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)
            ));
            object.add("deny", as(
                new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))
            ));

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)),
                    loader.getValues(object.get("default"))),
                () -> assertEquals(Collections.singleton(new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))),
                    loader.getValues(object.get("deny")))
            );
        }

        @Test
        void loadJsonWitchOnly1() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
            object.add("default", Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH).toJson());
            object.add("deny", new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD)).toJson());

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)),
                    loader.getValues(object.get("default"))),
                () -> assertEquals(Collections.singleton(new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))),
                    loader.getValues(object.get("deny")))
            );
        }

        @Test
        void loadJsonWitchOnly2() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            JsonObject object = new JsonObject();
            object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
            object.add("default", as(
                Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)
            ));
            object.add("deny", as(
                new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))
            ));
            Map<ResourceLocation, JsonElement> map = new HashMap<>();
            map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "witch_only"), object);
            loader.apply(map, ResourceManager.Empty.INSTANCE, InactiveProfiler.INSTANCE);

            assertAll(
                () -> assertEquals(Collections.singleton(Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)),
                    loader.getHolder().getDefaultConditions()),
                () -> assertEquals(Collections.singleton(new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))),
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

        @Test
        @DisplayName("From Json, Bat")
        void loadFromFile1() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Gson gson = new Gson();
            try (InputStream stream = getClass().getResourceAsStream("/data/limitmobspawn/limitmobspawn/no_bats.json");
                 Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                Map<ResourceLocation, JsonElement> map = new HashMap<>();
                map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "no_bats"), object);
                loader.apply(map, ResourceManager.Empty.INSTANCE, InactiveProfiler.INSTANCE);

                assertEquals(Collections.singleton(new EntityLimit(EntityType.BAT).and(new MobSpawnTypeLimit(MobSpawnType.SPAWN_EGG).not())),
                    loader.getHolder().getDenyConditions());
                assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
                assertTrue(loader.getHolder().getForceConditions().isEmpty());
            } catch (IOException e) {
                fail(e);
            }
        }

        @Test
        @DisplayName("From Json, Peaceful")
        void loadFromFile2() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Gson gson = new Gson();
            try (InputStream stream = getClass().getResourceAsStream("/data/limitmobspawn/limitmobspawn/peaceful.json");
                 Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                Map<ResourceLocation, JsonElement> map = new HashMap<>();
                map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "peaceful"), object);
                loader.apply(map, ResourceManager.Empty.INSTANCE, InactiveProfiler.INSTANCE);

                assertEquals(Collections.singleton(new MobCategoryLimit(MobCategory.MONSTER)),
                    loader.getHolder().getDenyConditions());
                assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
                assertTrue(loader.getHolder().getForceConditions().isEmpty());
            } catch (IOException e) {
                fail(e);
            }
        }

        @Test
        @DisplayName("From Json, Test1")
        void loadFromFile3() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Gson gson = new Gson();
            try (InputStream stream = getClass().getResourceAsStream("/data/limitmobspawn/limitmobspawn/test1.json");
                 Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                Map<ResourceLocation, JsonElement> map = new HashMap<>();
                map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "test1"), object);
                loader.apply(map, ResourceManager.Empty.INSTANCE, InactiveProfiler.INSTANCE);

                assertEquals(Sets.newHashSet(
                        Creator.entityAtDimension(Level.NETHER, EntityType.PIGLIN),
                        new MobCategoryLimit(MobCategory.CREATURE).or(new MobCategoryLimit(MobCategory.MISC)),
                        Creator.posAtDimension(Level.END, -500, 500, -500, 500)),
                    loader.getHolder().getDefaultConditions());
                assertEquals(Sets.newHashSet(
                        new DimensionLimit(Level.OVERWORLD),
                        new DimensionLimit(Level.NETHER),
                        new DimensionLimit(Level.END),
                        new EntityLimit(EntityType.BAT)),
                    loader.getHolder().getDenyConditions());
                assertTrue(loader.getHolder().getForceConditions().isEmpty());
            } catch (IOException e) {
                fail(e);
            }
        }

        @Test
        @DisplayName("Random Limit")
        void loadRandomLimit() {
            SpawnConditionLoader loader = SpawnConditionLoader.createInstance();
            Gson gson = new Gson();
            try (InputStream stream = getClass().getResourceAsStream("/data/limitmobspawn/limitmobspawn/cancel_70.json");
                 Reader reader = new InputStreamReader(Objects.requireNonNull(stream))) {
                JsonObject object = gson.fromJson(reader, JsonObject.class);
                Map<ResourceLocation, JsonElement> map = new HashMap<>();
                map.put(new ResourceLocation(LimitMobSpawn.MOD_ID, "cancel_70"), object);
                loader.apply(map, ResourceManager.Empty.INSTANCE, InactiveProfiler.INSTANCE);

                assertEquals(Collections.singleton(
                        new And(
                            new RandomLimit(0.7),
                            new Or(new MobSpawnTypeLimit(MobSpawnType.NATURAL), new MobSpawnTypeLimit(MobSpawnType.REINFORCEMENT))
                        )
                    ),
                    loader.getHolder().getDenyConditions());
                assertTrue(loader.getHolder().getForceConditions().isEmpty());
                assertTrue(loader.getHolder().getDefaultConditions().isEmpty());
            } catch (IOException e) {
                fail(e);
            }
        }
    }
}
