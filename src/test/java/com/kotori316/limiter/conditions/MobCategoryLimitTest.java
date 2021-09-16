package com.kotori316.limiter.conditions;

import java.util.Arrays;

import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.MobCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MobCategoryLimitTest extends BeforeAllTest {
    @Test
    void getReason() {
        assertAll(
            Arrays.stream(MobCategory.values())
                .map(r -> Pair.of(r, new MobCategoryLimit(r)))
                .map(p -> () -> assertEquals(p.getLeft(), p.getRight().getClassification(), p.toString()))
        );
    }

    @ParameterizedTest
    @EnumSource(MobCategory.class)
    void serializeNoError(MobCategory classification) {
        MobCategoryLimit limit = new MobCategoryLimit(classification);
        assertAll(
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.INSTANCE)),
            () -> assertDoesNotThrow(() -> limit.to(JsonOps.COMPRESSED)),
            () -> assertDoesNotThrow(() -> limit.to(NbtOps.INSTANCE))
        );
    }

    @ParameterizedTest
    @EnumSource(MobCategory.class)
    void cycleConsistency(MobCategory classification) {
        MobCategoryLimit limit = new MobCategoryLimit(classification);
        testCycle(limit);
    }
}
