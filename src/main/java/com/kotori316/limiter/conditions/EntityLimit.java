package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ForgeRegistries;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class EntityLimit implements TestSpawn {
    public static final TestSpawn.Serializer<EntityLimit> SERIALIZER = new Serializer();
    private final EntityType<?> type;
    private final ResourceLocation key; // Just for data creation. Not for runtime test.

    public EntityLimit(EntityType<?> type) {
        this.type = type;
        this.key = null;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", type);
    }

    /**
     * Just for data creation. Not for runtime. Use {@link EntityLimit#EntityLimit(EntityType)} instead.
     */
    public EntityLimit(String key) {
        this.type = EntityType.byKey(key).orElse(null);
        this.key = new ResourceLocation(key);
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", key);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return this.type.equals(entityTypeIn);
    }

    @Override
    public String toString() {
        return "EntityLimit{" +
            "type=" + type + '(' + type.getRegistryName() + ')' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.EntityLimit that = (com.kotori316.limiter.conditions.EntityLimit) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<EntityLimit> {
        @Override
        public String getType() {
            return "entity";
        }

        @Override
        public <T> EntityLimit from(Dynamic<T> dynamic) {
            EntityType<?> type = EntityType.byKey(dynamic.get("entity").asString("INVALID")).orElseThrow(() -> new RuntimeException("Error " + dynamic.getValue()));
            return new EntityLimit(type);
        }

        @Override
        public <T> T to(TestSpawn t, DynamicOps<T> ops) {
            EntityLimit l = (EntityLimit) t;
            Map<T, T> map = new HashMap<>();
            String value;
            if (l.type != null)
                value = EntityType.getKey(l.type).toString();
            else
                value = Objects.requireNonNull(l.key).toString();
            map.put(ops.createString("entity"), ops.createString(value));
            return ops.createMap(map);
        }

        @Override
        public Set<String> propertyKeys() {
            return Collections.singleton("entity");
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting) {
            if (property.equals("entity")) {
                return ForgeRegistries.ENTITIES.getKeys().stream().map(ResourceLocation::toString).collect(Collectors.toSet());
            }
            return Collections.emptySet();
        }
    }
}
