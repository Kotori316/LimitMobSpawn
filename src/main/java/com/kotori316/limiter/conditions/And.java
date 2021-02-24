package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class And implements TestSpawn {
    public static final TestSpawn.Serializer<And> SERIALIZER = new Serializer();
    private final TestSpawn t1;
    private final List<TestSpawn> ts;

    public And(TestSpawn t1, TestSpawn... ts) {
        this.t1 = t1;
        this.ts = Arrays.asList(ts);
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}, {}", t1, ts);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return t1.test(worldIn, pos, entityTypeIn, reason) &&
            ts.stream().allMatch(t -> t.test(worldIn, pos, entityTypeIn, reason));
    }

    @Override
    public TestSpawn and(TestSpawn other) {
        TestSpawn[] ts = this.ts.toArray(new TestSpawn[this.ts.size() + 1]);
        ts[this.ts.size()] = other;
        return new And(this.t1, ts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        And and = (And) o;
        return Objects.equals(t1, and.t1) && Objects.equals(ts, and.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, ts);
    }

    @Override
    public String toString() {
        return "And{" +
            "t1=" + t1 +
            ", ts=" + ts +
            '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<And> {

        @Override
        public String getType() {
            return "and";
        }

        @Override
        public And fromJson(JsonObject object) {
            List<TestSpawn> list = object.entrySet().stream()
                .filter(e -> e.getKey().startsWith("t"))
                .sorted(Comparator.comparing(e -> e.getKey().substring(1)))
                .map(e -> SpawnConditionLoader.INSTANCE.deserialize(e.getValue().getAsJsonObject()))
                .collect(Collectors.toList());
            if (list.size() < 1)
                throw new IllegalStateException("And object has no child conditions. " + object);
            return new And(list.get(0), list.subList(1, list.size()).toArray(new TestSpawn[0]));
        }

        @Override
        public JsonObject toJson(TestSpawn t) {
            And and = (And) t;
            JsonObject object = new JsonObject();
            object.add("t1", and.t1.toJson());
            for (int i = 0; i < and.ts.size(); i++) {
                object.add("t" + (i + 2), and.ts.get(i).toJson());
            }
            return object;
        }
    }
}
