package com.kotori316.limiter.capability;

import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
        var savedData = nbt.getAllKeys().stream()
            .map(MobNumberLimit::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Function.identity(), t -> nbt.getInt(t.getName())));
        this.map = savedData.isEmpty() ? new EnumMap<>(MobCategory.class) : new EnumMap<>(savedData);
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

    public Optional<Component> getMessage() {
        if (this.map.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(
                Component.literal(map.entrySet().stream()
                    .map(e -> "%s%s%s: %d".formatted(ChatFormatting.AQUA, e.getKey(), ChatFormatting.RESET, e.getValue()))
                    .collect(Collectors.joining("\n")))
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MobNumberLimit that = (MobNumberLimit) o;

        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Nullable
    @SuppressWarnings("ConstantConditions") // Actually, the return value might be null.
    private static MobCategory get(String s) {
        return MobCategory.byName(s);
    }
}
