package com.kotori316.limiter.conditions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockReader;
import org.junit.jupiter.api.Test;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.TestSpawn;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

class RandomLimitTest extends BeforeAllTest {

    @Test
    void isDeterministic() {
        TestSpawn testSpawn = new RandomLimit(0);
        assertFalse(testSpawn.isDeterministic());
    }

    @Test
    void test0() {
        RandomLimit limit = new RandomLimit(0);
        for (int i = 0; i < 10000; i++) {
            if (limit.test(EmptyBlockReader.INSTANCE, BlockPos.ZERO, null, null)) {
                fail("p is 0!, but succeeded");
            }
        }
    }

    @Test
    void test1() {
        RandomLimit limit = new RandomLimit(1);
        for (int i = 0; i < 10000; i++) {
            if (!limit.test(EmptyBlockReader.INSTANCE, BlockPos.ZERO, null, null)) {
                fail("p is 1!, but failed.");
            }
        }
    }
}
