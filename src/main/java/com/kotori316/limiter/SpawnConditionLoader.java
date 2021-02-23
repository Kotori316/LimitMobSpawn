package com.kotori316.limiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.EntityTypeLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;

public class SpawnConditionLoader extends JsonReloadListener {
    private static final Marker MARKER = MarkerManager.getMarker("LimitMobSpawn/SpawnConditionLoader");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<String, TestSpawn.Serializer<?>> serializers = new HashMap<>();
    public static final SpawnConditionLoader INSTANCE = new SpawnConditionLoader();

    private SpawnConditionLoader() {
        super(GSON, LimitMobSpawn.MOD_ID);
        register(TestSpawn.EMPTY_SERIALIZER);
        register(And.SERIALIZER);
        register(Or.SERIALIZER);
        register(Not.SERIALIZER);
        register(DimensionLimit.SERIALIZER);
        register(EntityLimit.SERIALIZER);
        register(EntityTypeLimit.SERIALIZER);
    }

    public void register(TestSpawn.Serializer<?> serializer) {
        this.serializers.put(serializer.getType(), serializer);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        Set<TestSpawn> denySet = new HashSet<>();
        Set<TestSpawn> defaultSet = new HashSet<>();
        Set<TestSpawn> forceSet = new HashSet<>();
        for (JsonElement element : objectIn.values()) {
            JsonObject asObject = element.getAsJsonObject();
            if (CraftingHelper.processConditions(asObject, "conditions")) {
                defaultSet.addAll(getValues(asObject.get("default")));
                denySet.addAll(getValues(asObject.get("deny")));
                forceSet.addAll(getValues(asObject.get("force")));
            }
        }
        LimitMobSpawn.defaultSet = defaultSet;
        LimitMobSpawn.denySet = denySet;
        LimitMobSpawn.forceSet = forceSet;
    }

    private Set<TestSpawn> getValues(JsonElement element) {
        if (element == null) return Collections.emptySet();
        if (element.isJsonArray()) {
            return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(this::deserialize)
                .collect(Collectors.toSet());
        } else if (element.isJsonObject()) {
            return element.getAsJsonObject().entrySet().stream()
                .map(Map.Entry::getValue)
                .map(JsonElement::getAsJsonObject)
                .map(this::deserialize)
                .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public TestSpawn deserialize(JsonObject object) {
        String type = JSONUtils.getString(object, "type", "anonymous");
        TestSpawn.Serializer<?> serializer = serializers.get(type);
        if (serializer == null || serializer == TestSpawn.EMPTY_SERIALIZER) {
            LimitMobSpawn.LOGGER.error(MARKER, "Type {} is not registered. Error in loading {}", type, object);
            return TestSpawn.Empty.INSTANCE;
        }
        return serializer.fromJson(object);
    }
}
