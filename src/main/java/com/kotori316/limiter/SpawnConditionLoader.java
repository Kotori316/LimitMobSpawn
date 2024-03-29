package com.kotori316.limiter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.VisibleForTesting;

import com.kotori316.limiter.capability.LMSDataPackHolder;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.BiomeLimit;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.LightLevelLimit;
import com.kotori316.limiter.conditions.MobCategoryLimit;
import com.kotori316.limiter.conditions.MobSpawnTypeLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.RandomLimit;

public class SpawnConditionLoader extends SimpleJsonResourceReloadListener {
    private static final Marker MARKER = MarkerManager.getMarker("SpawnConditionLoader");
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private final Map<String, TestSpawn.Serializer<?>> serializers = new HashMap<>();
    public static final SpawnConditionLoader INSTANCE = new SpawnConditionLoader();
    private final LMSDataPackHolder holder = new LMSDataPackHolder();
    @VisibleForTesting
    static boolean SKIP_CONDITION = System.getProperty("limit_mob_spawn_data_gen") != null;

    private SpawnConditionLoader() {
        super(GSON, LimitMobSpawn.MOD_ID);
        register(TestSpawn.EMPTY_SERIALIZER);
        register(All.SERIALIZER);
        register(And.SERIALIZER);
        register(Or.SERIALIZER);
        register(Not.SERIALIZER);
        register(DimensionLimit.SERIALIZER);
        register(EntityLimit.SERIALIZER);
        register(MobCategoryLimit.SERIALIZER);
        register(PositionLimit.SERIALIZER);
        register(MobSpawnTypeLimit.SERIALIZER);
        register(BiomeLimit.SERIALIZER);
        register(RandomLimit.SERIALIZER);
        register(LightLevelLimit.SERIALIZER);
    }

    // ---------- Data pack Serialize & Deserialize ----------
    public void register(TestSpawn.Serializer<?> serializer) {
        TestSpawn.Serializer<?> put = this.serializers.put(serializer.getType(), serializer);
        if (put != null)
            throw new IllegalArgumentException(String.format("Duplicated keys: %s, TYPE: %s, Map: %s", serializer.getType(), serializer.getClass(), serializers));
        LimitMobSpawn.LOGGER.debug(MARKER, "Registered a new serializer. {}", serializer.getClass().getName());
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        Set<TestSpawn> denySet = new HashSet<>();
        Set<TestSpawn> defaultSet = new HashSet<>();
        Set<TestSpawn> forceSet = new HashSet<>();
        for (JsonElement element : objectIn.values()) {
            JsonObject asObject = element.getAsJsonObject();
            ICondition.IContext context;
            if (holder.context != null) {
                context = holder.context;
            } else {
                if (!SKIP_CONDITION) LimitMobSpawn.LOGGER.warn(MARKER, "holder.context should not be null.");
                context = ICondition.IContext.EMPTY;
            }
            if (SKIP_CONDITION || CraftingHelper.processConditions(asObject, "conditions", context)) {
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

    public void setContext(ICondition.IContext context) {
        this.holder.context = context;
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
            if (GsonHelper.isValidNode(element.getAsJsonObject(), "type")) {
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
            if (SKIP_CONDITION) {
                // Throw exception in test.
                throw new IllegalArgumentException("Type %s is not registered. Error in loading %s".formatted(type, dynamic.getValue()));
            } else {
                LimitMobSpawn.LOGGER.error(MARKER, "Type {} is not registered. Error in loading {}", type, dynamic.getValue());
                return TestSpawn.Empty.INSTANCE;
            }
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
