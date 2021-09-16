package com.kotori316.limiter.capability;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.common.util.INBTSerializable;

public class SpawnerControl implements INBTSerializable<CompoundTag> {
    public static final String KEY_SPAWN_COUNT = "spawnCount";
    private int spawnCount = 0;

    public SpawnerControl() {
    }

    public Optional<Integer> getSpawnCount() {
        return spawnCount > 0 ? Optional.of(spawnCount) : Optional.empty();
    }

    public void setSpawnCount(int spawnCount) {
        this.spawnCount = spawnCount;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(KEY_SPAWN_COUNT, spawnCount);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        setSpawnCount(nbt.getInt(KEY_SPAWN_COUNT));
    }

    @Override
    public String toString() {
        return "SpawnerControl{" +
            "spawnCount=" + spawnCount +
            '}';
    }

    public List<TextComponent> getMessages() {
        return Arrays.asList(
            new TextComponent("SpawnerControl"),
            new TextComponent("SpawnCount: " + getSpawnCount())
        );
    }
}
