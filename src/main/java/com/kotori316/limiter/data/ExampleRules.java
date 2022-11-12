package com.kotori316.limiter.data;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.MobSpawnTypeLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;

import static com.kotori316.limiter.data.Rules.as;

@SuppressWarnings("unused")
final class ExampleRules {
    static void addAll(List<Pair<String, JsonElement>> list) {
        ExampleRules rules = new ExampleRules();
        Rules.generateJson(list, rules);
    }

    JsonObject no_bats() {
        JsonObject object = new JsonObject();
        object.addProperty("_comment", "Prevent spawning of bats, except from Spawn Egg ans Spawner.");
        object.add("deny", as(
            new EntityLimit(EntityType.BAT).and(new Or(new MobSpawnTypeLimit(MobSpawnType.SPAWN_EGG), new MobSpawnTypeLimit(MobSpawnType.SPAWNER)).not())
        ));
        JsonArray conditions = new JsonArray();
        conditions.add(TrueCondition.Serializer.INSTANCE.getJson(TrueCondition.INSTANCE));
        object.add("conditions", conditions);
        return object;
    }

    JsonObject no_trader() {
        JsonObject object = new JsonObject();
        object.add("deny", as(
            new And(
                new Or(
                    new EntityLimit(EntityType.WANDERING_TRADER),
                    new EntityLimit(EntityType.TRADER_LLAMA)
                ),
                new Not(new Or(
                    new MobSpawnTypeLimit(MobSpawnType.SPAWN_EGG),
                    new MobSpawnTypeLimit(MobSpawnType.SPAWNER)
                ))
            )
        ));
        JsonArray conditions = new JsonArray();
        conditions.add(ModLoadedCondition.Serializer.INSTANCE.getJson(new ModLoadedCondition(LimitMobSpawn.MOD_ID)));
        object.add("conditions", conditions);
        return object;
    }
}
