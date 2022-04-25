package com.kotori316.limiter.capability;

import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobNumberLimitTest {
    MobNumberLimit mobNumberLimit;
    CompoundTag tag;

    @BeforeEach
    void setup() {
        mobNumberLimit = new MobNumberLimit();
        mobNumberLimit.set(MobCategory.MONSTER, 15);
        mobNumberLimit.set(MobCategory.CREATURE, 30);
        tag = new CompoundTag();
        tag.putInt(MobCategory.MONSTER.getName(), 15);
        tag.putInt(MobCategory.CREATURE.getName(), 30);
    }

    @Test
    void serializeNBT() {
        var serialized = mobNumberLimit.serializeNBT();
        assertEquals(tag, serialized);
    }

    @Test
    void deserializeNBT() {
        var m = new MobNumberLimit();
        assertNotEquals(mobNumberLimit, m);
        m.deserializeNBT(tag);
        assertEquals(mobNumberLimit, m);
    }

    @Test
    void firstIsEmpty() {
        var m = new MobNumberLimit();
        assertAll(
            Arrays.stream(MobCategory.values()).map(m::getLimit)
                .map(o -> () -> assertTrue(o.isEmpty()))
        );
    }
}
