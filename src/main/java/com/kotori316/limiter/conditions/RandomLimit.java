package com.kotori316.limiter.conditions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import com.kotori316.limiter.TestSpawn;

public class RandomLimit implements TestSpawn {
    public static final TestSpawn.Serializer<RandomLimit> SERIALIZER = new Serializer();
    private final double p;
    private final Random random;

    public RandomLimit(double p) {
        this.p = p;
        this.random = new Random();
        if (p < 0 || 1 < p) {
            throw new IllegalArgumentException("p doesn't satisfy 0 <= p <= 1, p=" + p);
        }
    }

    @Override
    public boolean test(IBlockReader worldIn, BlockPos pos, EntityType<?> entityTypeIn, @Nullable SpawnReason reason) {
        return random.nextDouble() < p;
    }

    @Override
    public TestSpawn.Serializer<? extends TestSpawn> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public String contentShort() {
        return "random " + this.p;
    }

    @Override
    public String toString() {
        return "Random{p=" + this.p + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RandomLimit that = (RandomLimit) o;
        return Double.compare(that.p, this.p) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.p);
    }

    @Override
    public RandomLimit not() {
        return new RandomLimit(1 - p);
    }

    @Override
    public boolean isDeterministic() {
        return false;
    }

    private static class Serializer extends TestSpawn.Serializer<RandomLimit> {

        @Override
        public String getType() {
            return "random";
        }

        @Override
        public <T> RandomLimit from(Dynamic<T> dynamic) {
            double p = dynamic.get("p").asNumber().map(Number::doubleValue)
                .result()
                .orElseGet(() -> Double.parseDouble(dynamic.get("p").asString("1")));
            return new RandomLimit(p);
        }

        @Override
        public <T> T to(TestSpawn a, DynamicOps<T> ops) {
            Map<T, T> map = new HashMap<>();
            map.put(ops.createString("p"), ops.createDouble(((RandomLimit) a).p));
            return ops.createMap(map);
        }

        @Override
        public Set<String> propertyKeys() {
            return Collections.singleton("p");
        }

        @Override
        public Set<String> possibleValues(String property, boolean suggesting, @Nullable ISuggestionProvider provider) {
            return Collections.emptySet();
        }
    }
}
