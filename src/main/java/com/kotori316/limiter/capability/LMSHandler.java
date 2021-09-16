package com.kotori316.limiter.capability;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public interface LMSHandler extends INBTSerializable<CompoundTag> {
    void addDefaultCondition(TestSpawn condition);

    void addDenyCondition(TestSpawn condition);

    void addForceCondition(TestSpawn condition);

    Set<TestSpawn> getDefaultConditions();

    Set<TestSpawn> getDenyConditions();

    Set<TestSpawn> getForceConditions();

    void clearDefaultConditions();

    void clearDenyConditions();

    void clearForceConditions();

    SpawnerControl getSpawnerControl();

    @Override
    default CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        Collector<Tag, ?, ListTag> arrayCollector = Collector.of(ListTag::new, ListTag::add, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }, Collector.Characteristics.IDENTITY_FINISH);
        for (RuleType ruleType : RuleType.values()) {
            nbt.put(ruleType.saveName(), ruleType.getRules(this).stream().map(t -> t.to(NbtOps.INSTANCE)).collect(arrayCollector));
        }
        nbt.put("SpawnerControl", getSpawnerControl().serializeNBT());
        return nbt;
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        for (RuleType ruleType : RuleType.values()) {
            nbt.getList(ruleType.saveName(), Constants.NBT.TAG_COMPOUND).stream()
                .map(n -> new Dynamic<>(NbtOps.INSTANCE, n))
                .map(SpawnConditionLoader.INSTANCE::deserialize)
                .forEach(t -> ruleType.add(this, t));
        }
        getSpawnerControl().deserializeNBT(nbt.getCompound("SpawnerControl"));
    }

    static void registerCapability() {
        LMSCapability cap = new LMSCapability();
        CapabilityManager.INSTANCE.register(LMSHandler.class);
    }

    static Stream<TestSpawn> getCombinedDefault(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getDefaultConditions().stream(), h2.map(LMSHandler::getDefaultConditions).map(Set::stream).orElse(Stream.empty()));
    }

    static Stream<TestSpawn> getCombinedDeny(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getDenyConditions().stream(), h2.map(LMSHandler::getDenyConditions).map(Set::stream).orElse(Stream.empty()));
    }

    static Stream<TestSpawn> getCombinedForce(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getForceConditions().stream(), h2.map(LMSHandler::getForceConditions).map(Set::stream).orElse(Stream.empty()));
    }
}

final class LMSCapability implements Callable<LMSHandler> {

    @Override
    public LMSHandler call() {
        return new LMSConditionsHolder();
    }
}
