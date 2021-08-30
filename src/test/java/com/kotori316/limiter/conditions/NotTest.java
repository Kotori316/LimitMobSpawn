package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
        return Stream.of(JsonOps.COMPRESSED, JsonOps.INSTANCE, NBTDynamicOps.INSTANCE);
    }

    @Test
    void dummy() {
        assertTrue(opsStream().findAny().isPresent());
    }

    @ParameterizedTest
    @MethodSource("opsStream")
    <T> void cycleConsistency(DynamicOps<T> ops) {
        assertAll(Stream.of(new DimensionLimit(World.THE_NETHER), new EntityClassificationLimit(EntityClassification.CREATURE), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new PositionLimit(new BlockPos(-10, 5, 64), new BlockPos(24, 65, 95)))
            .map(Not::new)
            .map(n -> () -> assertEquals(n, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic<>(ops, n.to(ops))))));
    }
}
