package com.kotori316.limiter.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinationTest extends BeforeAllTest {
    static final TestSpawn[] testSpawns = {new DimensionLimit(World.THE_NETHER),
        new EntityClassificationLimit(EntityClassification.CREATURE),
        new SpawnReasonLimit(SpawnReason.SPAWNER),
        new PositionLimit(new BlockPos(-10, 5, 64), new BlockPos(24, 65, 95)),
        new PositionLimit(-15, 263, 2, 45, 624, 964),
        new EntityLimit("minecraft:zombie"),
        new EntityClassificationLimit(EntityClassification.WATER_CREATURE),
        new DimensionLimit(World.THE_END),
        new SpawnReasonLimit(SpawnReason.CHUNK_GENERATION)};

    static List<TestSpawn[]> get2Conditions() {
        List<TestSpawn[]> list = new ArrayList<>();
        for (int i = 0; i < testSpawns.length; i++) {
            for (int j = 0; j < testSpawns.length; j++) {
                if (i != j)
                    list.add(new TestSpawn[]{testSpawns[i], testSpawns[j]});
            }
        }
        return list;
    }

    static List<TestSpawn[]> get3Conditions() {
        List<TestSpawn[]> list = new ArrayList<>();
        for (int i = 0; i < testSpawns.length; i++) {
            for (int j = 0; j < testSpawns.length; j++) {
                if (i != j) {
                    for (int k = 0; k < testSpawns.length; k++) {
                        if (i != k)
                            list.add(new TestSpawn[]{testSpawns[i], testSpawns[j], testSpawns[k]});
                    }
                }
            }
        }
        return list;
    }

    @Test
    void dummy() {
        assertTrue(get2Conditions().size() > 0);
        assertTrue(get3Conditions().size() > 0);
    }

    @ParameterizedTest
    @MethodSource("get2Conditions")
    void and2CycleConsistency(TestSpawn t1, TestSpawn t2) {
        TestSpawn limit = new And(t1, t2);
        testCycle(limit);
    }

    @ParameterizedTest
    @MethodSource("get2Conditions")
    void or2CycleConsistency(TestSpawn t1, TestSpawn t2) {
        Or limit = new Or(t1, t2);
        testCycle(limit);
    }

    @ParameterizedTest
    @MethodSource("get3Conditions")
    void and3CycleConsistency(TestSpawn t1, TestSpawn t2, TestSpawn t3) {
        And limit = new And(t1, t2, t3);
        testCycle(limit);
    }

    @ParameterizedTest
    @MethodSource("get3Conditions")
    void or3CycleConsistency(TestSpawn t1, TestSpawn t2, TestSpawn t3) {
        Or limit = new Or(t1, t2, t3);
        testCycle(limit);
    }

    @ParameterizedTest
    @MethodSource("get2Conditions")
    void andFromObjectAndArray(TestSpawn t1, TestSpawn t2) {
        JsonObject object1 = new JsonObject();
        object1.addProperty("type", "and");
        object1.add("t1", t1.toJson());
        object1.add("t2", t2.toJson());

        JsonObject object2 = new JsonObject();
        object2.addProperty("type", "and");
        JsonArray array = new JsonArray();
        array.add(t1.toJson());
        array.add(t2.toJson());
        object2.add("values", array);

        And and = new And(t1, t2);
        assertAll(
            () -> assertEquals(and, SpawnConditionLoader.INSTANCE.deserialize(object1)),
            () -> assertEquals(and, SpawnConditionLoader.INSTANCE.deserialize(object2))
        );
    }

    @ParameterizedTest
    @MethodSource("get2Conditions")
    void orFromObjectAndArray(TestSpawn t1, TestSpawn t2) {
        JsonObject object1 = new JsonObject();
        object1.addProperty("type", "or");
        object1.add("t1", t1.toJson());
        object1.add("t2", t2.toJson());

        JsonObject object2 = new JsonObject();
        object2.addProperty("type", "or");
        JsonArray array = new JsonArray();
        array.add(t1.toJson());
        array.add(t2.toJson());
        object2.add("values", array);

        Or or = new Or(t1, t2);
        assertAll(
            () -> assertEquals(or, SpawnConditionLoader.INSTANCE.deserialize(object1)),
            () -> assertEquals(or, SpawnConditionLoader.INSTANCE.deserialize(object2))
        );
    }

    @Test
    void createAndFromList1() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER));
        TestSpawn expect = new And(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER));
        assertEquals(expect, new And(list), String.format("And from %s", list));
    }

    @Test
    void createAndFromList2() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        TestSpawn expect = new And(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        assertEquals(expect, new And(list), String.format("And from %s", list));
    }

    @Test
    void andShortString() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        String shortString = new And(list).contentShort();
        assertAll(list.stream().map(TestSpawn::contentShort).map(s ->
            () -> assertTrue(shortString.contains(s), "Contains " + s + " in " + shortString)));
    }

    @Test
    void orShortString() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        String shortString = new Or(list).contentShort();
        assertAll(list.stream().map(TestSpawn::contentShort).map(s ->
            () -> assertTrue(shortString.contains(s), "Contains " + s + " in " + shortString)));
    }

    @Test
    void createOrFromList1() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER));
        TestSpawn expect = new Or(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER));
        assertEquals(expect, new Or(list), String.format("And from %s", list));
    }

    @Test
    void createOrFromList2() {
        List<TestSpawn> list = Arrays.asList(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        TestSpawn expect = new Or(new DimensionLimit(World.THE_NETHER), new SpawnReasonLimit(SpawnReason.SPAWNER),
            new EntityClassificationLimit(EntityClassification.CREATURE));
        assertEquals(expect, new Or(list), String.format("And from %s", list));
    }
}
