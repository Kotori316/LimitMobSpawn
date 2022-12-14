package com.kotori316.limiter.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.limiter.LimitMobSpawn;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LimitMobSpawn.MOD_ID)
public class LimitMobSpawnDataProvider {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new TestSpawnProvider(event.getGenerator()));
        event.getGenerator().addProvider(event.includeServer(), new ExampleRuleProvider(event.getGenerator()));
    }

    private record TestSpawnProvider(DataGenerator dataGenerator) implements DataProvider {

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            Path parent = dataGenerator.getPackOutput().getOutputFolder().resolve("data/" + LimitMobSpawn.MOD_ID + "/" + LimitMobSpawn.MOD_ID);
            return CompletableFuture.allOf(getData().stream().map(pair ->
                DataProvider.saveStable(cache, pair.getRight(), parent.resolve(pair.getLeft() + ".json"))
            ).toArray(CompletableFuture[]::new));
        }

        @Override
        public String getName() {
            return getClass().getName();
        }

        private List<Pair<String, JsonElement>> getData() {
            List<Pair<String, JsonElement>> list = new ArrayList<>();
            Rules.addAll(list);
            return list;
        }
    }

    private record ExampleRuleProvider(DataGenerator dataGenerator) implements DataProvider {

        @Override
        public CompletableFuture<?> run(CachedOutput cache) {
            Path parent = dataGenerator.getPackOutput().getOutputFolder().resolve("data/" + "lms_example" + "/" + LimitMobSpawn.MOD_ID);
            return CompletableFuture.allOf(getData().stream().map(pair ->
                DataProvider.saveStable(cache, pair.getRight(), parent.resolve(pair.getLeft() + ".json"))
            ).toArray(CompletableFuture[]::new));
        }

        @Override
        public String getName() {
            return getClass().getName();
        }

        private List<Pair<String, JsonElement>> getData() {
            List<Pair<String, JsonElement>> list = new ArrayList<>();
            ExampleRules.addAll(list);
            return list;
        }
    }
}
