package com.kotori316.limiter.capability;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import net.minecraft.nbt.CompoundNBT;

import com.kotori316.limiter.TestSpawn;

public class LMSDataPackHolder implements LMSHandler {
    private Set<TestSpawn> denySet = new HashSet<>();
    private Set<TestSpawn> defaultSet = new HashSet<>();
    private Set<TestSpawn> forceSet = new HashSet<>();

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
    public CompoundNBT serializeNBT() {
        return new CompoundNBT();
    }

    @Override
    @Deprecated // The set directly replaced to loaded data.
    public void deserializeNBT(CompoundNBT nbt) {
    }

    @Override
    public String toString() {
        return "LMSDataPackHolder{defaultSet: " + defaultSet.size()
            + ", denySet: " + denySet.size()
            + ", forceSet: " + forceSet.size()
            + '}';
    }
}
