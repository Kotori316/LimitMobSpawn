package com.kotori316.limiter.data;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.Creator;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.LightLevelLimit;
import com.kotori316.limiter.conditions.MobCategoryLimit;
import com.kotori316.limiter.conditions.MobSpawnTypeLimit;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.RandomLimit;

@SuppressWarnings("unused")
class Rules {
    static void addAll(List<Pair<String, JsonElement>> list) {
        Rules rules = new Rules();
        Class<?>[] classes = {};
        generateJson(list, rules);
    }

    static void generateJson(List<Pair<String, JsonElement>> list, Object rules) {
        Arrays.stream(rules.getClass().getDeclaredMethods())
            .filter(m -> (m.getModifiers() & Modifier.STATIC) == 0)
            .filter(m -> m.getParameterTypes().length == 0)
            .filter(m -> m.getReturnType() == JsonObject.class)
            .forEach(m -> {
                LimitMobSpawn.LOGGER.info("Generate with {}", m.getName());
                m.setAccessible(true);
                try {
                    list.add(Pair.of(m.getName().toLowerCase(Locale.ROOT), ((JsonObject) m.invoke(rules))));
                } catch (ReflectiveOperationException e) {
                    LimitMobSpawn.LOGGER.error(e);
                }
            });
    }

    static JsonArray as(TestSpawn... conditions) {
        JsonArray array = new JsonArray();
        for (TestSpawn spawn : conditions) {
            array.add(spawn.toJson());
        }
        return array;
    }

    JsonObject test1() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Conditions in each category are combined by OR.");
        {
            object.add("default", as(
                Creator.entityAtDimension(Level.NETHER, EntityType.PIGLIN),
                new MobCategoryLimit(MobCategory.CREATURE).or(new MobCategoryLimit(MobCategory.MISC)),
                Creator.posAtDimension(Level.END, -500, 500, -500, 500)
            ));
        }
        {
            object.add("deny", as(
                new DimensionLimit(Level.OVERWORLD),
                new DimensionLimit(Level.NETHER),
                new DimensionLimit(Level.END),
                new EntityLimit(EntityType.BAT)
            ));
        }
        {
            JsonArray conditions = new JsonArray();
            conditions.add(TrueCondition.Serializer.INSTANCE.getJson(TrueCondition.INSTANCE));
            object.add("conditions", conditions);
        }
        return object;
    }

    JsonObject test3() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Conditions in each category are combined by OR.");
        {
            object.add("default", as(
                new MobSpawnTypeLimit(MobSpawnType.SPAWNER),
                new MobSpawnTypeLimit(MobSpawnType.SPAWN_EGG),
                new DimensionLimit(Level.OVERWORLD).not().and(new EntityLimit(EntityType.GHAST))
            ));
        }
        {
            object.add("deny", as(
                new DimensionLimit(Level.OVERWORLD),
                new DimensionLimit(Level.NETHER),
                new DimensionLimit(Level.END)
            ));
        }
        {
            object.add("force", as(
                Creator.posAtDimension(Level.OVERWORLD, -64, 64, -64, 64)
                    .and(new EntityLimit(EntityType.ENDERMAN)),
                Creator.posAtDimension(Level.OVERWORLD, 64, 128, -64, 64)
                    .and(new EntityLimit(EntityType.WITCH))
            ));
        }
        {
            JsonArray conditions = new JsonArray();
            conditions.add(TrueCondition.Serializer.INSTANCE.getJson(TrueCondition.INSTANCE));
            object.add("conditions", conditions);
        }
        return object;
    }

    JsonObject peaceful() {
        JsonObject object = new JsonObject();
        {
            object.add("deny", as(
                new MobCategoryLimit(MobCategory.MONSTER)
            ));
        }
        {
            JsonArray conditions = new JsonArray();
            conditions.add(TrueCondition.Serializer.INSTANCE.getJson(TrueCondition.INSTANCE));
            object.add("conditions", conditions);
        }
        return object;
    }

    JsonObject no_bats() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Prevent spawning of bats, except from Spawn Egg.");
        object.add("deny", as(
            new EntityLimit(EntityType.BAT).and(new MobSpawnTypeLimit(MobSpawnType.SPAWN_EGG).not())
        ));
        return object;
    }

    JsonObject witch_only() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "In overworld, only witch spawns at night. Other monsters disappeared.");
        object.add("default", as(
            Creator.entityAtDimension(Level.OVERWORLD, EntityType.WITCH)
        ));
        object.add("deny", as(
            new MobCategoryLimit(MobCategory.MONSTER).and(new DimensionLimit(Level.OVERWORLD))
        ));
        return object;
    }

    @SuppressWarnings("SpellCheckingInspection")
    JsonObject enderman_only() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "You'll see only Endermans in the world!");
        object.add("force", as(new EntityLimit(EntityType.ENDERMAN)));
        object.add("deny", as(All.getInstance()));
        return object;
    }

    JsonObject cancel_spawner() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Cancel all spawns from Monster Spawner");
        object.add("deny", as(new MobSpawnTypeLimit(MobSpawnType.SPAWNER)));
        return object;
    }

    JsonObject cancel_70() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Cancel 70% of spawning.");
        object.add("deny", as(new And(
            new RandomLimit(0.7),
            new Or(new MobSpawnTypeLimit(MobSpawnType.NATURAL), new MobSpawnTypeLimit(MobSpawnType.REINFORCEMENT))
        )));
        return object;
    }

    JsonObject mining_dim() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Stop spawning of Zombie and Skeleton in 'mining_dimension', which is modded dimension.");
        {
            object.add("deny", as(
                new And(
                    new DimensionLimit(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("mining_dimension:mining"))),
                    new Or(
                        new EntityLimit(EntityType.ZOMBIE),
                        new EntityLimit(EntityType.SKELETON)
                    )
                )
            ));
        }
        {
            JsonArray conditions = new JsonArray();
            conditions.add(ModLoadedCondition.Serializer.INSTANCE.getJson(new ModLoadedCondition("mining_dimension")));
            object.add("conditions", conditions);
        }
        return object;
    }

    @SuppressWarnings("SpellCheckingInspection")
    JsonObject gaia_dim() {
        JsonObject object = new JsonObject();
        {
            object.add("deny", as(
                new DimensionLimit(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("gaiadimension:gaia_dimension")))
                    .and(new Or(
                        new EntityLimit("gaiadimension:agate_golem            ".trim()),
                        new EntityLimit("gaiadimension:ancient_lagrahk        ".trim()),
                        new EntityLimit("gaiadimension:archaic_warrior        ".trim()),
                        new EntityLimit("gaiadimension:blue_howlite_wolf      ".trim()),
                        new EntityLimit("gaiadimension:cavern_tick            ".trim()),
                        new EntityLimit("gaiadimension:contorted_naga         ".trim()),
                        new EntityLimit("gaiadimension:corrupt_sapper         ".trim()),
                        new EntityLimit("gaiadimension:lesser_shockshooter    ".trim()),
                        new EntityLimit("gaiadimension:lesser_spitfire        ".trim()),
                        new EntityLimit("gaiadimension:malachite_drone        ".trim()),
                        new EntityLimit("gaiadimension:malachite_guard        ".trim()),
                        new EntityLimit("gaiadimension:muckling               ".trim()),
                        new EntityLimit("gaiadimension:primal_beast           ".trim()),
                        new EntityLimit("gaiadimension:shalurker              ".trim()),
                        new EntityLimit("gaiadimension:bismuth_uletrus        ".trim()),
                        new EntityLimit("gaiadimension:crystal_golem          ".trim()),
                        new EntityLimit("gaiadimension:growth_sapper          ".trim()),
                        new EntityLimit("gaiadimension:howlite_wolf           ".trim()),
                        new EntityLimit("gaiadimension:mutant_growth_extractor".trim()),
                        new EntityLimit("gaiadimension:nomadic_lagrahk        ".trim()),
                        new EntityLimit("gaiadimension:rocky_luggeroth        ".trim()),
                        new EntityLimit("gaiadimension:rugged_lurmorus        ".trim()),
                        new EntityLimit("gaiadimension:saltion                ".trim()),
                        new EntityLimit("gaiadimension:spellbound_elemental   ".trim()),
                        new EntityLimit("gaiadimension:markuzar_plant         ".trim()),
                        new EntityLimit("gaiadimension:mineral_arenthis       ".trim()),
                        new EntityLimit("gaiadimension:shallow_arenthis       ".trim()),
                        new EntityLimit("gaiadimension:agate_arrow            ".trim()),
                        new EntityLimit("gaiadimension:thrown_pebble          ".trim())))
            ));
        }
        {
            JsonArray conditions = new JsonArray();
            conditions.add(ModLoadedCondition.Serializer.INSTANCE.getJson(new ModLoadedCondition("gaiadimension")));
            object.add("conditions", conditions);
        }
        return object;
    }

    JsonObject allow_only_0() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Stop monsters from spawning in areas whose light level is over 0. As 1.18 do.");
        object.add("deny", as(new And(
            new MobCategoryLimit(MobCategory.MONSTER),
            new LightLevelLimit(LightLayer.BLOCK, 0)
        )));
        return object;
    }

    JsonObject prevent_natural_monster() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Prevent monsters from spawning naturally.");
        object.add("deny", as(new And(
            new MobCategoryLimit(MobCategory.MONSTER),
            new Or(new MobSpawnTypeLimit(MobSpawnType.NATURAL), new MobSpawnTypeLimit(MobSpawnType.CHUNK_GENERATION))
        )));
        return object;
    }

    JsonObject underground_only() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Hostile monsters only in underground");
        object.add("deny", as(new And(
            new MobCategoryLimit(MobCategory.MONSTER),
            new PositionLimit(-Level.MAX_LEVEL_SIZE, 64, -Level.MAX_LEVEL_SIZE, Level.MAX_LEVEL_SIZE, Level.MAX_ENTITY_SPAWN_Y, Level.MAX_LEVEL_SIZE)
        )));
        return object;
    }
}
