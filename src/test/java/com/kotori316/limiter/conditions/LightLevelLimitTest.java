package com.kotori316.limiter.conditions;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.world.level.LightLayer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LightLevelLimitTest extends BeforeAllTest {
    @Test
    void dummy() {
        assertTrue(cycleTest().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("cycleTest")
    void cycleConsistency(LightLayer layer, int level) {
        LightLevelLimit limit = new LightLevelLimit(layer, level);
        testCycle(limit);
    }

    static Stream<Object[]> cycleTest() {
        return Stream.of(LightLayer.values()).flatMap(layer ->
            IntStream.rangeClosed(-2, 17)
                .mapToObj(level -> new Object[]{layer, level}));
    }
}
