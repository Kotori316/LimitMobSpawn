package com.kotori316.limiter.conditions;

import java.util.Objects;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import com.kotori316.limiter.TestSpawn;

public class DimensionLimit implements TestSpawn {
    public static final TestSpawn.Serializer<DimensionLimit> SERIALIZER = new Serializer();
    private final RegistryKey<World> type;

    public DimensionLimit(RegistryKey<World> type) {
        this.type = type;
    }

    @Override
    public boolean test(IWorldReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        RegistryKey<World> type;
        if (worldIn instanceof World) {
            World world = (World) worldIn;
            type = world.getDimensionKey();
        } else {
            type = World.OVERWORLD;
        }
        return this.type == type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        com.kotori316.limiter.conditions.DimensionLimit that = (com.kotori316.limiter.conditions.DimensionLimit) o;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public String toString() {
        return "DimensionLimit{" + "type=" + type + '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<DimensionLimit> {
        @Override
        public String getType() {
            return "dimension";
        }

        @Override
        public DimensionLimit fromJson(JsonObject object) {
            RegistryKey<World> dim = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(JSONUtils.getString(object, "dim")));
            return new DimensionLimit(dim);
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            DimensionLimit l = (DimensionLimit) t;
            JsonObject object = new JsonObject();
            object.addProperty("dim", l.type.getLocation().toString());
            return object;
        }
    }
}
