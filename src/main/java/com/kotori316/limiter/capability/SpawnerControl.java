package com.kotori316.limiter.capability;

import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public class SpawnerControl implements INBTSerializable<CompoundNBT> {
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
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("spawnCount", spawnCount);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        setSpawnCount(nbt.getInt("spawnCount"));
    }

    @Override
    public String toString() {
        return "SpawnerControl{" +
            "spawnCount=" + spawnCount +
            '}';
    }
}
