package com.kotori316.limiter.capability;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundTag;

import com.kotori316.limiter.TestSpawn;

public class LMSDataPackHolder implements LMSHandler {
    private Set<TestSpawn> denySet = Collections.emptySet();
    private Set<TestSpawn> defaultSet = Collections.emptySet();
    private Set<TestSpawn> forceSet = Collections.emptySet();

    @Override
    @Deprecated // This class is not for each append.
    public void addDefaultCondition(TestSpawn condition) {
    }

    @Override
    @Deprecated // This class is not for each append.
    public void addDenyCondition(TestSpawn condition) {
    }

    @Override
    @Deprecated // This class is not for each append.
    public void addForceCondition(TestSpawn condition) {
    }

    @Override
    @Deprecated // This class is modified by data packs, not commands.
    public void clearDefaultConditions() {
    }

    @Override
    @Deprecated // This class is modified by data packs, not commands.
    public void clearDenyConditions() {
    }

    @Override
    @Deprecated // This class is modified by data packs, not commands.
    public void clearForceConditions() {
    }

    @Override
    public SpawnerControl getSpawnerControl() {
        return new SpawnerControl();
    }

    @Override
    public MobNumberLimit getMobNumberLimit() {
        return new MobNumberLimit();
    }

    @Override
    public Set<TestSpawn> getDefaultConditions() {
        return defaultSet;
    }

    @Override
    public Set<TestSpawn> getDenyConditions() {
        return denySet;
    }

    @Override
    public Set<TestSpawn> getForceConditions() {
        return forceSet;
    }

    public void setDenySet(@Nonnull Set<TestSpawn> denySet) {
        this.denySet = denySet;
    }

    public void setDefaultSet(@Nonnull Set<TestSpawn> defaultSet) {
        this.defaultSet = defaultSet;
    }

    public void setForceSet(@Nonnull Set<TestSpawn> forceSet) {
        this.forceSet = forceSet;
    }

    @Override
    @Deprecated // No need as data pack saves the data.
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    @Deprecated // The set directly replaced to loaded data.
    public void deserializeNBT(CompoundTag nbt) {
    }

    @Override
    public String toString() {
        return "LMSDataPackHolder{defaultSet: " + defaultSet.size()
            + ", denySet: " + denySet.size()
            + ", forceSet: " + forceSet.size()
            + '}';
    }
}
