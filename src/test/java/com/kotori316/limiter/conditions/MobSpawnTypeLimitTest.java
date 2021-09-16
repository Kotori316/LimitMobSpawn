package com.kotori316.limiter.conditions;

import java.util.Arrays;

import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.EmptyBlockGetter;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobSpawnTypeLimitTest extends BeforeAllTest {
    @Test
    void getReason() {
        assertAll(
            Arrays.stream(MobSpawnType.values())
                .map(r -> Pair.of(r, new MobSpawnTypeLimit(r)))
                .map(p -> () -> assertEquals(p.getLeft(), p.getRight().reason(), p.toString()))
        );
    }

    @ParameterizedTest
    @EnumSource(MobSpawnType.class)
    void serializeNoError(MobSpawnType reason) {
        MobSpawnTypeLimit limit = new MobSpawnTypeLimit(reason);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NbtOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @EnumSource(MobSpawnType.class)
    void cycleConsistency(MobSpawnType reason) {
        MobSpawnTypeLimit limit = new MobSpawnTypeLimit(reason);
        testCycle(limit);
    }

    @Test
    void reasonTest() {
        MobSpawnTypeLimit limit = new MobSpawnTypeLimit(MobSpawnType.NATURAL);

        assertAll(
            () -> assertTrue(limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, EntityType.DOLPHIN, MobSpawnType.NATURAL)),
            () -> assertFalse(limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, EntityType.DOLPHIN, MobSpawnType.SPAWNER)),
            () -> assertFalse(limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, EntityType.DOLPHIN, MobSpawnType.SPAWN_EGG)),
            () -> assertFalse(limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, EntityType.DOLPHIN, null))
        );
    }
}
