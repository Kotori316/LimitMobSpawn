package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class And implements TestSpawn {
    public static final TestSpawn.Serializer<And> SERIALIZER = new Serializer();
    private static final Pattern KEY_PATTERN = Pattern.compile("t(\\d+)");
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
        StringBuilder builder = new StringBuilder("And{t1=" + t1);
        for (int i = 0; i < ts.size(); i++) {
            builder.append(", t").append(i + 2).append('=').append(ts.get(i));
        }
        builder.append('}');
        return builder.toString();
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
        public <T> And from(Dynamic<T> dynamic) {
            Map<String, Dynamic<T>> stringDynamicMap = dynamic.asMap(d -> d.asString(""), Function.identity());
            List<TestSpawn> list;
            if (!stringDynamicMap.isEmpty())
                list = stringDynamicMap
                    .entrySet().stream()
                    .filter(e -> KEY_PATTERN.matcher(e.getKey()).matches())
                    .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(1))))
                    .map(e -> SpawnConditionLoader.INSTANCE.deserialize(e.getValue()))
                    .collect(Collectors.toList());
            else
                // dynamic has array of values
                list = dynamic.asList(SpawnConditionLoader.INSTANCE::deserialize);
            if (list.size() < 1)
                throw new IllegalStateException("And object has no child conditions. " + dynamic.getValue());
            return new And(list.get(0), list.subList(1, list.size()).toArray(new TestSpawn[0]));
        }

        @Override
        public <T> T to(TestSpawn t, DynamicOps<T> ops) {
            And and = (And) t;
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("t1"), and.t1.to(ops));
            for (int i = 0; i < and.ts.size(); i++) {
                map.put(ops.createString("t" + (i + 2)), and.ts.get(i).to(ops));
            }
            return ops.createMap(map);
        }
    }
}
