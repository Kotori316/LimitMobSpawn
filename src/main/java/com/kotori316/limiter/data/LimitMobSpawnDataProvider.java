package com.kotori316.limiter.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.limiter.LimitMobSpawn;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LimitMobSpawn.MOD_ID)
public class LimitMobSpawnDataProvider {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new TestSpawnProvider(event.getGenerator()));
    }

    private record TestSpawnProvider(DataGenerator dataGenerator) implements DataProvider {

        @Override
        public void run(CachedOutput cache) throws IOException {
            Path parent = dataGenerator.getOutputFolder().resolve("data/" + LimitMobSpawn.MOD_ID + "/" + LimitMobSpawn.MOD_ID);
            for (Pair<String, JsonElement> pair : getData()) {
                DataProvider.saveStable(cache, pair.getRight(), parent.resolve(pair.getLeft() + ".json"));
            }
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
}
