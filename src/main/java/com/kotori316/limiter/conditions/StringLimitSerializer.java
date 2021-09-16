package com.kotori316.limiter.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;

import com.kotori316.limiter.TestSpawn;

public abstract class StringLimitSerializer<T extends TestSpawn, Value> extends TestSpawn.Serializer<T> {
    public abstract Value fromString(String s);

    public abstract String valueToString(Value value);

    public abstract String saveKey();

    public abstract T instance(Value value);

    public abstract Value getter(T t);

    @Override
    public Set<String> propertyKeys() {
        return Collections.singleton(saveKey());
    }

    @Override
    public <T1> T from(Dynamic<T1> dynamic) {
        String valueString = dynamic.get(saveKey()).asString("INVALID");
        Value value = fromString(valueString);
        if (value == null) {
            throw new IllegalArgumentException("Value is null, by input: " + valueString + ", whole: " + dynamic.getValue());
        }
        return instance(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1 to(TestSpawn a, DynamicOps<T1> ops) {
        Value value = getter(((T) a));
        String valueString = valueToString(value);
        Map<T1, T1> map = new HashMap<>();
        map.put(ops.createString(saveKey()), ops.createString(valueString));
        return ops.createMap(map);
    }

    public static <Type extends TestSpawn, Value> StringLimitSerializer<Type, Value> fromFunction(
        Function<Type, Value> getter, Function<Value, Type> instance,
        Function<Value, String> asString, Function<String, Value> fromString,
        String saveKey, String typeName
    ) {
        return fromFunction(getter, instance, asString, fromString, saveKey, typeName, () -> null);
    }

    public static <Type extends TestSpawn, Value> StringLimitSerializer<Type, Value> fromFunction(
        Function<Type, Value> getter, Function<Value, Type> instance,
        Function<Value, String> asString, Function<String, Value> fromString,
        String saveKey, String typeName, @Nullable Value[] values
    ) {
        return fromFunction(getter, instance, asString, fromString, saveKey, typeName, () -> values);
    }

    public static <Type extends TestSpawn, Value> StringLimitSerializer<Type, Value> fromFunction(
        Function<Type, Value> getter, Function<Value, Type> instance,
        Function<Value, String> asString, Function<String, Value> fromString,
        String saveKey, String typeName, @Nonnull Supplier<Value[]> valueSupplier
    ) {
        return new StringLimitSerializer<>() {
            @Override
            public Value fromString(String s) {
                return fromString.apply(s);
            }

            @Override
            public String valueToString(Value value) {
                return asString.apply(value);
            }

            @Override
            public String saveKey() {
                return saveKey;
            }

            @Override
            public Type instance(Value value) {
                return instance.apply(value);
            }

            @Override
            public Value getter(Type type) {
                return getter.apply(type);
            }

            @Override
            public String getType() {
                return typeName;
            }

            @Override
            public Set<String> possibleValues(String property, boolean suggesting, SharedSuggestionProvider provider) {
                Value[] values = valueSupplier.get();
                if (values != null && property.equals(saveKey())) {
                    if (!suggesting && Enum.class.isAssignableFrom(values.getClass().getComponentType())) {
                        return Arrays.stream(values).flatMap(v -> Stream.of(valueToString(v), ((Enum<?>) v).name())).collect(Collectors.toSet());
                    } else {
                        return Arrays.stream(values).map(this::valueToString).collect(Collectors.toSet());
                    }
                }
                return Collections.emptySet();
            }
        };
    }
}
