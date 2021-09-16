package com.kotori316.limiter.conditions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.TestSpawn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class RandomLimitTest extends BeforeAllTest {

    @Test
    void isDeterministic() {
        TestSpawn testSpawn = new RandomLimit(0);
        assertFalse(testSpawn.isDeterministic());
    }

    @Test
    void equal1() {
        TestSpawn t1 = new RandomLimit(0.6);
        TestSpawn t2 = new RandomLimit(0.6);
        assertEquals(t1, t2);
    }

    @Test
    void not() {
        TestSpawn t1 = new RandomLimit(0.6);
        TestSpawn not1 = t1.not();
        assertEquals(new RandomLimit(0.4), not1);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, 1.5, -0.1, 1.1})
    void badInstance(double p) {
        assertThrows(IllegalArgumentException.class, () -> new RandomLimit(p));
    }

    @Test
    void test0() {
        RandomLimit limit = new RandomLimit(0);
        for (int i = 0; i < 10000; i++) {
            if (limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, null, null)) {
                fail("p is 0!, but succeeded");
            }
        }
    }

    @Test
    void test1() {
        RandomLimit limit = new RandomLimit(1);
        for (int i = 0; i < 10000; i++) {
            if (!limit.test(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, null, null)) {
                fail("p is 1!, but failed.");
            }
        }
    }
}
