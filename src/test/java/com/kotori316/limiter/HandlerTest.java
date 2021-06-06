package com.kotori316.limiter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.Sets;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.kotori316.limiter.capability.LMSConditionsHolder;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.Creator;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.EntityClassificationLimit;
import com.kotori316.limiter.conditions.EntityLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandlerTest extends BeforeAllTest {
    private LMSHandler lmsHandler;

    @BeforeEach
    void setup() {
        lmsHandler = new LMSConditionsHolder();
        lmsHandler.addDefaultCondition(Creator.posAtDimension(World.OVERWORLD, BlockPos.ZERO, new BlockPos(64, 256, 64)));
        lmsHandler.addDenyCondition(new DimensionLimit(World.THE_NETHER));
        lmsHandler.addDenyCondition(new EntityLimit(EntityType.BAT));
        lmsHandler.addForceCondition(new EntityLimit(EntityType.ENDERMAN));
    }

    @Test
    void getDefault() {
        assertAll(
            () -> assertEquals(Collections.singleton(Creator.posAtDimension(World.OVERWORLD, BlockPos.ZERO, new BlockPos(64, 256, 64))),
                lmsHandler.getDefaultConditions()),
            () -> assertEquals(lmsHandler.getDefaultConditions(), RuleType.DEFAULT.getRules(lmsHandler))
        );
    }

    @Test
    void getDeny() {
        assertAll(
            () -> assertEquals(Sets.newHashSet(new DimensionLimit(World.THE_NETHER), new EntityLimit(EntityType.BAT)), lmsHandler.getDenyConditions()),
            () -> assertEquals(lmsHandler.getDenyConditions(), RuleType.DENY.getRules(lmsHandler))
        );
    }

    @Test
    void getForce() {
        assertAll(
            () -> assertEquals(Collections.singleton(new EntityLimit(EntityType.ENDERMAN)), lmsHandler.getForceConditions()),
            () -> assertEquals(lmsHandler.getForceConditions(), RuleType.FORCE.getRules(lmsHandler))
        );
    }

    @Test
    void addDefault() {
        TestSpawn a = All.getInstance();
        lmsHandler.addDefaultCondition(a);
        assertTrue(lmsHandler.getDefaultConditions().contains(a));
        assertTrue(RuleType.DEFAULT.getRules(lmsHandler).contains(a));
        assertFalse(lmsHandler.getDenyConditions().contains(a));
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void add(RuleType ruleType) {
        TestSpawn a = All.getInstance();
        ruleType.add(lmsHandler, a);

        assertTrue(ruleType.getRules(lmsHandler).contains(a));
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void addAll(RuleType ruleType) {
        List<TestSpawn> a = Arrays.asList(All.getInstance(), new EntityClassificationLimit(EntityClassification.CREATURE), new SpawnReasonLimit(SpawnReason.SPAWNER));
        ruleType.addAll(lmsHandler, a);

        assertTrue(ruleType.getRules(lmsHandler).containsAll(a));
    }

    @ParameterizedTest
    @EnumSource(RuleType.class)
    void removeAll(RuleType ruleType) {
        ruleType.removeAll(lmsHandler);
        assertTrue(ruleType.getRules(lmsHandler).isEmpty());
        assertFalse(Arrays.stream(RuleType.values()).filter(Predicate.isEqual(ruleType).negate())
            .map(r -> r.getRules(lmsHandler))
            .anyMatch(Set::isEmpty));
    }
}
