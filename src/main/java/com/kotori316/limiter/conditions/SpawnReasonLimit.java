package com.kotori316.limiter.conditions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
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
        public <T> SpawnReasonLimit from(Dynamic<T> dynamic) {
            String reasonName = dynamic.get("spawn_reason").asString("");
            SpawnReason spawnReason = SpawnReason.valueOf(reasonName.toUpperCase(Locale.ROOT));
            return new SpawnReasonLimit(spawnReason);
        }

        @Override
        public <T> T to(TestSpawn a, DynamicOps<T> ops) {
            SpawnReasonLimit spawnReasonLimit = ((SpawnReasonLimit) a);
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("spawn_reason"), ops.createString(spawnReasonLimit.reason.toString().toLowerCase(Locale.ROOT)));
            return ops.createMap(map);
        }
    }
}
