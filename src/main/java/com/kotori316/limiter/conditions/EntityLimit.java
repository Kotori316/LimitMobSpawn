package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class EntityLimit implements TestSpawn {
    public static final TestSpawn.Serializer<EntityLimit> SERIALIZER = new Serializer();
    private final EntityType<?> type;

    public EntityLimit(EntityType<?> type) {
        this.type = type;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", type);
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
        public EntityLimit fromJson(JsonObject object) {
            EntityType<?> type = EntityType.byKey(JSONUtils.getString(object, "entity")).orElseThrow(() -> new RuntimeException("Error " + object));
            return new EntityLimit(type);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            EntityLimit l = (EntityLimit) t;
            JsonObject object = new JsonObject();
            object.addProperty("entity", EntityType.getKey(l.type).toString());
            return object;
        }
    }
}
