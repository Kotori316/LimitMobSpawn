package com.kotori316.limiter.conditions;

import java.util.Arrays;

import com.mojang.serialization.JsonOps;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NBTDynamicOps;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SpawnReasonLimitTest extends BeforeAllTest {
    @Test
    void getReason() {
        assertAll(
            Arrays.stream(SpawnReason.values())
                .map(r -> Pair.of(r, new SpawnReasonLimit(r)))
                .map(p -> () -> assertEquals(p.getLeft(), p.getRight().getReason(), p.toString()))
        );
    }

    @ParameterizedTest
    @EnumSource(SpawnReason.class)
    void serializeNoError(SpawnReason reason) {
        SpawnReasonLimit limit = new SpawnReasonLimit(reason);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NBTDynamicOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @EnumSource(SpawnReason.class)
    void cycleConsistency(SpawnReason reason) {
        SpawnReasonLimit limit = new SpawnReasonLimit(reason);
        testCycle(limit);
    }
}
