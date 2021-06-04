package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.command.ISuggestionProvider;
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

    public And(List<TestSpawn> list) {
        this.t1 = list.get(0);
        this.ts = list.subList(1, list.size());
        LimitMobSpawn.LOGGER.debug(TestSpawn.MARKER, getClass().getSimpleName() + " Instance created with {}", list);
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

    @Override
    public String contentShort() {
        StringBuilder builder = new StringBuilder("And{" + t1.contentShort());
        for (TestSpawn t : ts) {
            builder.append(", ").append(t.contentShort());
        }
        builder.append('}');
        return builder.toString();
    }

    private static class Serializer extends TestSpawn.Serializer<And> {

        @Override
        public String getType() {
            return "and";
        }

        @Override
        public <T> And from(Dynamic<T> dynamic) {
            List<TestSpawn> list = dynamic.get("values").map(d -> d.asList(SpawnConditionLoader.INSTANCE::deserialize)).result()
                .orElseGet(() ->
                    dynamic.asMap(d -> d.asString(""), Function.identity()).entrySet().stream()
                        .filter(e -> KEY_PATTERN.matcher(e.getKey()).matches())
                        .sorted(Comparator.comparing(e -> Integer.parseInt(e.getKey().substring(1))))
                        .map(e -> SpawnConditionLoader.INSTANCE.deserialize(e.getValue()))
                        .collect(Collectors.toList()));
            if (list.size() < 1)
                throw new IllegalStateException("And object has no child conditions. " + dynamic.getValue());
            return new And(list.get(0), list.subList(1, list.size()).toArray(new TestSpawn[0]));
        }

        @Override
        public <T> T to(TestSpawn t, DynamicOps<T> ops) {
            And and = (And) t;
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("values"),
                ops.createList(Stream.concat(Stream.of(and.t1), and.ts.stream()).map(ts -> ts.to(ops))));
            return ops.createMap(map);
        }

        @Override
        public Set<String> propertyKeys() {
            return Collections.emptySet();
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, ISuggestionProvider provider) {
            return Collections.emptySet();
        }
    }
}
