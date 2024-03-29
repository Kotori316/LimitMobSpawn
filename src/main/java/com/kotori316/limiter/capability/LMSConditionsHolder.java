package com.kotori316.limiter.capability;

import java.util.HashSet;
import java.util.Set;

import com.kotori316.limiter.TestSpawn;

public class LMSConditionsHolder implements LMSHandler {
    private final Set<TestSpawn> defaultConditions = new HashSet<>(), denyConditions = new HashSet<>(), forceConditions = new HashSet<>();
    private final SpawnerControl spawnerControl = new SpawnerControl();
    private final MobNumberLimit mobNumberLimit = new MobNumberLimit();

    @Override
    public void addDefaultCondition(TestSpawn condition) {
        defaultConditions.add(condition);
    }

    @Override
    public void addDenyCondition(TestSpawn condition) {
        denyConditions.add(condition);
    }

    @Override
    public void addForceCondition(TestSpawn condition) {
        forceConditions.add(condition);
    }

    @Override
    public Set<TestSpawn> getDefaultConditions() {
        return defaultConditions;
    }

    @Override
    public Set<TestSpawn> getDenyConditions() {
        return denyConditions;
    }

    @Override
    public Set<TestSpawn> getForceConditions() {
        return forceConditions;
    }

    @Override
    public void clearDefaultConditions() {
        defaultConditions.clear();
    }

    @Override
    public void clearDenyConditions() {
        denyConditions.clear();
    }

    @Override
    public void clearForceConditions() {
        forceConditions.clear();
    }

    @Override
    public SpawnerControl getSpawnerControl() {
        return spawnerControl;
    }

    @Override
    public MobNumberLimit getMobNumberLimit() {
        return mobNumberLimit;
    }

    @Override
    public String toString() {
        return "LMSConditionsHolder{defaultConditions: " + defaultConditions.size()
            + ", denyConditions: " + denyConditions.size()
            + ", forceConditions: " + forceConditions.size()
            + '}';
    }
}
