package com.kotori316.limiter.conditions;

import java.util.Arrays;

import com.mojang.serialization.JsonOps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.nbt.NBTDynamicOps;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EntityClassificationLimitTest extends BeforeAllTest {
    @Test
    void getReason() {
        assertAll(
            Arrays.stream(EntityClassification.values())
                .map(r -> Pair.of(r, new EntityClassificationLimit(r)))
                .map(p -> () -> assertEquals(p.getLeft(), p.getRight().getClassification(), p.toString()))
        );
    }

    @ParameterizedTest
    @EnumSource(EntityClassification.class)
    void serializeNoError(EntityClassification classification) {
        EntityClassificationLimit limit = new EntityClassificationLimit(classification);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NBTDynamicOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @EnumSource(EntityClassification.class)
    void cycleConsistency(EntityClassification classification) {
        EntityClassificationLimit limit = new EntityClassificationLimit(classification);
        testCycle(limit);
    }
}
