package com.kotori316.limiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import com.kotori316.limiter.capability.LMSDataPackHolder;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityClassificationLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

public class SpawnConditionLoader extends JsonReloadListener {
    private static final Marker MARKER = MarkerManager.getMarker("SpawnConditionLoader");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<String, TestSpawn.Serializer<?>> serializers = new HashMap<>();
    public static final SpawnConditionLoader INSTANCE = new SpawnConditionLoader();
    private final LMSDataPackHolder holder = new LMSDataPackHolder();
    private static final boolean SKIP_CONDITION =
        !FMLLoader.isProduction() && System.getenv("target") == null;

    private SpawnConditionLoader() {
        super(GSON, LimitMobSpawn.MOD_ID);
        register(TestSpawn.EMPTY_SERIALIZER);
        register(All.SERIALIZER);
        register(And.SERIALIZER);
        register(Or.SERIALIZER);
        register(Not.SERIALIZER);
        register(DimensionLimit.SERIALIZER);
        register(EntityLimit.SERIALIZER);
        register(EntityClassificationLimit.SERIALIZER);
        register(PositionLimit.SERIALIZER);
        register(SpawnReasonLimit.SERIALIZER);
    }

    // ---------- Data pack Serialize & Deserialize ----------
    public void register(TestSpawn.Serializer<?> serializer) {
        TestSpawn.Serializer<?> put = this.serializers.put(serializer.getType(), serializer);
        if (put != null)
            throw new IllegalArgumentException(String.format("Duplicated keys: %s, TYPE: %s, Map: %s", serializer.getType(), serializer.getClass(), serializers));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
        Set<TestSpawn> denySet = new HashSet<>();
        Set<TestSpawn> defaultSet = new HashSet<>();
        Set<TestSpawn> forceSet = new HashSet<>();
        for (JsonElement element : objectIn.values()) {
            JsonObject asObject = element.getAsJsonObject();
            if (SKIP_CONDITION || CraftingHelper.processConditions(asObject, "conditions")) {
                defaultSet.addAll(getValues(asObject.get(RuleType.DEFAULT.saveName())));
                denySet.addAll(getValues(asObject.get(RuleType.DENY.saveName())));
                forceSet.addAll(getValues(asObject.get(RuleType.FORCE.saveName())));
            }
        }
        holder.setDefaultSet(defaultSet);
        holder.setDenySet(denySet);
        holder.setForceSet(forceSet);
    }

    public LMSHandler getHolder() {
        return holder;
    }

    @VisibleForTesting
        // Should be private
    Set<TestSpawn> getValues(JsonElement element) {
        if (element == null) return Collections.emptySet();
        if (element.isJsonArray()) {
            return StreamSupport.stream(element.getAsJsonArray().spliterator(), false)
                .map(JsonElement::getAsJsonObject)
                .map(this::deserialize)
                .collect(Collectors.toSet());
        } else if (element.isJsonObject()) {
            if (JSONUtils.hasField(element.getAsJsonObject(), "type")) {
                return Collections.singleton(this.deserialize(element.getAsJsonObject()));
            } else {
                return element.getAsJsonObject().entrySet().stream()
                    .map(Map.Entry::getValue)
                    .map(JsonElement::getAsJsonObject)
                    .map(this::deserialize)
                    .collect(Collectors.toSet());
            }
        }
        return Collections.emptySet();
    }

    public TestSpawn deserialize(JsonObject object) {
        return deserialize(new Dynamic<>(JsonOps.INSTANCE, object));
    }

    public <T> TestSpawn deserialize(Dynamic<T> dynamic) {
        String type = dynamic.get("type").asString("anonymous");
        TestSpawn.Serializer<?> serializer = serializers.get(type);
        if (serializer == null || serializer == TestSpawn.EMPTY_SERIALIZER) {
            LimitMobSpawn.LOGGER.error(MARKER, "Type {} is not registered. Error in loading {}", type, dynamic.getValue());
            return TestSpawn.Empty.INSTANCE;
        }
        return serializer.from(dynamic);
    }

    // ---------- Command ----------
    public boolean hasSerializeKey(String key) {
        return this.serializers.containsKey(key);
    }

    public TestSpawn.Serializer<?> getSerializer(String key) {
        return this.serializers.get(key);
    }

    public Set<String> serializeKeySet() {
        Set<String> inactive = Sets.newHashSet("anonymous");
        return this.serializers.keySet().stream().filter(s -> !inactive.contains(s)).collect(Collectors.toSet());
    }

    @VisibleForTesting // Should not exist.
    static SpawnConditionLoader createInstance() {
        return new SpawnConditionLoader();
    }
}
