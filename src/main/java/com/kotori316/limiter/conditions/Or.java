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

public class Or implements TestSpawn {
    public static final TestSpawn.Serializer<Or> SERIALIZER = new Serializer();
    private static final Pattern KEY_PATTERN = Pattern.compile("t(\\d+)");
    private final TestSpawn t1;
    private final List<TestSpawn> ts;

    public Or(TestSpawn t1, TestSpawn... ts) {
        this.t1 = t1;
        this.ts = Arrays.asList(ts);
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}, {}", t1, ts);
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, SpawnReason reason) {
        return t1.test(worldIn, pos, entityTypeIn, reason) || ts.stream().anyMatch(t -> t.test(worldIn, pos, entityTypeIn, reason));
    }

    @Override
    public TestSpawn or(TestSpawn other) {
        TestSpawn[] ts = this.ts.toArray(new TestSpawn[this.ts.size() + 1]);
        ts[this.ts.size()] = other;
        return new Or(this.t1, ts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Or or = (Or) o;
        return Objects.equals(t1, or.t1) && Objects.equals(ts, or.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1, ts);
    }

    @Override
    public String toString() {
        return "Or{" +
            "t1=" + t1 +
            ", ts=" + ts +
            '}';
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer extends TestSpawn.Serializer<Or> {

        @Override
        public String getType() {
            return "or";
        }
        @Override
        public <T> Or from(Dynamic<T> dynamic) {
            List<TestSpawn> list = dynamic.asMap(d -> d.asString(""), Function.identity())
                .entrySet().stream()
                .filter(e -> KEY_PATTERN.matcher(e.getKey()).matches())
                .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(1))))
                .map(e -> SpawnConditionLoader.INSTANCE.deserialize(e.getValue()))
                .collect(Collectors.toList());
            if (list.size() < 1)
                throw new IllegalStateException("And object has no child conditions. " + dynamic.getValue());
            return new Or(list.get(0), list.subList(1, list.size()).toArray(new TestSpawn[0]));
        }

        @Override
        public <T> T to(TestSpawn t, DynamicOps<T> ops) {
            Or or = (Or) t;
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("t1"), or.t1.to(ops));
            for (int i = 0; i < or.ts.size(); i++) {
                map.put(ops.createString("t" + (i + 2)), or.ts.get(i).to(ops));
            }
            return ops.createMap(map);
        }
    }
}
