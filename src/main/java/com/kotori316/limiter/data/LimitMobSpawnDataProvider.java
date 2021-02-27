package com.kotori316.limiter.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.commons.lang3.tuple.Pair;

import com.kotori316.limiter.LimitMobSpawn;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LimitMobSpawn.MOD_ID)
public class LimitMobSpawnDataProvider {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        if (event.includeServer()) {
            event.getGenerator().addProvider(new TestSpawnProvider(event.getGenerator()));
        }
    }

    private static class TestSpawnProvider implements IDataProvider {
        private final DataGenerator dataGenerator;

        private TestSpawnProvider(DataGenerator dataGenerator) {
            this.dataGenerator = dataGenerator;
        }

        @Override
        public void act(DirectoryCache cache) throws IOException {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
            Path parent = dataGenerator.getOutputFolder().resolve("data/" + LimitMobSpawn.MOD_ID + "/" + LimitMobSpawn.MOD_ID);
            for (Pair<String, JsonElement> pair : getData()) {
                IDataProvider.save(gson, cache, pair.getRight(), parent.resolve(pair.getLeft() + ".json"));
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
