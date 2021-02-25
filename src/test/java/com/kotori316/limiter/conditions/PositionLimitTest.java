package com.kotori316.limiter.conditions;

import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.SpawnConditionLoader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PositionLimitTest extends BeforeAllTest {

    static Stream<BlockPos[]> poses() {
        return Stream.of(
            Pair.of(BlockPos.ZERO, BlockPos.ZERO),
            Pair.of(new BlockPos(-9546212, 26, 2456211), new BlockPos(562, 32, 56)),
            Pair.of(new BlockPos(-654, 256, 3654), new BlockPos(489654, 3, 24)),
            Pair.of(new BlockPos(24, 5, 96), new BlockPos(9, 365, 2456121)),
            Pair.of(new BlockPos(4875, 3, 4), new BlockPos(365, -1, -956)),
            Pair.of(new BlockPos(6589542, 24, 30525), new BlockPos(-558, 4, -85212))
        ).map(p -> new BlockPos[]{p.getLeft(), p.getRight()});
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @ParameterizedTest
    @MethodSource("poses")
    void consistency(BlockPos pos1, BlockPos pos2) {
        PositionLimit limit = new PositionLimit(pos1, pos2);
        assertAll(Stream.of(JsonOps.INSTANCE, JsonOps.COMPRESSED, NBTDynamicOps.INSTANCE).map(
            ops -> () -> assertEquals(limit, SpawnConditionLoader.INSTANCE.deserialize(new Dynamic(ops, limit.to(ops))))
        ));
    }
}
