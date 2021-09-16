package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.SpawnConditionLoader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotTest extends BeforeAllTest {
    static Stream<DynamicOps<?>> opsStream() {
        return Stream.of(JsonOps.COMPRESSED, JsonOps.INSTANCE, NbtOps.INSTANCE);
    }

    @Test
    void dummy() {
        assertTrue(opsStream().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("opsStream")
    <T> void cycleConsistency(DynamicOps<T> ops) {
        assertAll(Stream.of(new DimensionLimit(Level.NETHER), new MobCategoryLimit(MobCategory.CREATURE), new MobSpawnTypeLimit(MobSpawnType.SPAWNER),
            new PositionLimit(new BlockPos(-10, 5, 64), new BlockPos(24, 65, 95)))
            .map(Not::new)
            .map(n -> () -> assertEquals(n, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(ops, n.to(ops))))));
    }
}
