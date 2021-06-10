package com.kotori316.limiter.capability;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.limiter.BeforeAllTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SpawnerControlTest extends BeforeAllTest {

    @Test
    void serializeNBT() {
        SpawnerControl control = new SpawnerControl();
        control.setSpawnCount(10);
        CompoundNBT nbt = control.serializeNBT();
        assertEquals(10, nbt.getInt(SpawnerControl.KEY_SPAWN_COUNT));
    }

    @Test
    void deserializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        {
            nbt.putInt(SpawnerControl.KEY_SPAWN_COUNT, 15);
        }
        SpawnerControl control = new SpawnerControl();
        control.deserializeNBT(nbt);
        assertEquals(Optional.of(15), control.getSpawnCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5})
    void getCount1(int newCount) {
        SpawnerControl control = new SpawnerControl();
        control.setSpawnCount(newCount);
        assertEquals(Optional.of(newCount), control.getSpawnCount());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -5})
    void getCount2(int newCount) {
        SpawnerControl control = new SpawnerControl();
        control.setSpawnCount(newCount);
        assertFalse(control.getSpawnCount().isPresent());
    }
}
