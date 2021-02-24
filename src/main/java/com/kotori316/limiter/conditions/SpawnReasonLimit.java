package com.kotori316.limiter.conditions;

import java.util.Locale;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public class SpawnReasonLimit implements TestSpawn {
    public static final TestSpawn.Serializer<SpawnReasonLimit> SERIALIZER = new Serializer();
    private final SpawnReason reason;

    public SpawnReasonLimit(SpawnReason reason) {
        this.reason = reason;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", reason);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        // Pass if reason isn't available.
        return reason == null || reason == this.reason;
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<SpawnReasonLimit> {

        @Override
        public String getType() {
            return "spawn_reason";
        }

        @Override
        public SpawnReasonLimit fromJson(JsonObject object) {
            String reasonName = JSONUtils.getString(object, "spawn_reason");
            SpawnReason spawnReason = SpawnReason.valueOf(reasonName.toUpperCase(Locale.ROOT));
            return new SpawnReasonLimit(spawnReason);
        }

        @Override
        public JsonObject toJson(TestSpawn a) {
            SpawnReasonLimit spawnReasonLimit = ((SpawnReasonLimit) a);
            JsonObject object = new JsonObject();
            object.addProperty("spawn_reason", spawnReasonLimit.reason.toString().toLowerCase(Locale.ROOT));
            return object;
        }
    }
}
