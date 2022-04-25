package com.kotori316.limiter.capability;

import java.util.EnumMap;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public final class MobNumberLimit implements INBTSerializable<CompoundTag> {
    private EnumMap<MobCategory, Integer> map = new EnumMap<>(MobCategory.class);

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        map.forEach((mobCategory, integer) -> tag.putInt(mobCategory.getName(), integer));
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.map = new EnumMap<>(
            nbt.getAllKeys().stream()
                .map(MobNumberLimit::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Function.identity(), t -> nbt.getInt(t.getName())))
        );
    }

    public OptionalInt getLimit(MobCategory mobCategory) {
        if (this.map.containsKey(mobCategory)) {
            return OptionalInt.of(this.map.get(mobCategory));
        } else {
            return OptionalInt.empty();
        }
    }

    public void set(MobCategory mobCategory, int limit) {
        this.map.put(mobCategory, limit);
    }

    @Nullable
    @SuppressWarnings("ConstantConditions") // Actually, the return value might be null.
    private static MobCategory get(String s) {
        return MobCategory.byName(s);
    }
}
