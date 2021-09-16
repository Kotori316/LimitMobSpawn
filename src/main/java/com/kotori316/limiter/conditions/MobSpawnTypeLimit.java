package com.kotori316.limiter.conditions;

import java.util.Locale;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.BlockGetter;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;

public record MobSpawnTypeLimit(MobSpawnType reason) implements TestSpawn {
    public static final TestSpawn.Serializer<MobSpawnTypeLimit> SERIALIZER = StringLimitSerializer.fromFunction(
        MobSpawnTypeLimit::reason, MobSpawnTypeLimit::new, r -> r.toString().toLowerCase(Locale.ROOT),
        s -> MobSpawnType.valueOf(s.toUpperCase(Locale.ROOT)), "spawn_reason", "spawn_reason",
        MobSpawnType.values()
    );

    public MobSpawnTypeLimit(MobSpawnType reason) {
        this.reason = reason;
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", reason);
    }

    @Override
    public boolean test(BlockGetter worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable MobSpawnType reason) {
        // Pass if reason isn't available.
        return reason != null && reason == this.reason;
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "by " + reason.name();
    }

}
