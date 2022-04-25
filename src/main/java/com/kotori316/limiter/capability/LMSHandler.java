package com.kotori316.limiter.capability;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
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

    MobNumberLimit getMobNumberLimit();

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
        nbt.put("MobNumberLimit", getMobNumberLimit().serializeNBT());
        return nbt;
    }

    @Override
    default void deserializeNBT(CompoundTag nbt) {
        for (RuleType ruleType : RuleType.values()) {
            nbt.getList(ruleType.saveName(), Tag.TAG_COMPOUND).stream()
                .map(n -> new Dynamic<>(NbtOps.INSTANCE, n))
                .map(SpawnConditionLoader.INSTANCE::deserialize)
                .forEach(t -> ruleType.add(this, t));
        }
        getSpawnerControl().deserializeNBT(nbt.getCompound("SpawnerControl"));
        getMobNumberLimit().deserializeNBT(nbt.getCompound("MobNumberLimit"));
    }

    static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(LMSHandler.class);
    }

    static Stream<TestSpawn> getCombinedDefault(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getDefaultConditions().stream(), h2.map(LMSHandler::getDefaultConditions).stream().flatMap(Collection::stream));
    }

    static Stream<TestSpawn> getCombinedDeny(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getDenyConditions().stream(), h2.map(LMSHandler::getDenyConditions).stream().flatMap(Collection::stream));
    }

    static Stream<TestSpawn> getCombinedForce(LMSHandler h1, LazyOptional<LMSHandler> h2) {
        return Stream.concat(h1.getForceConditions().stream(), h2.map(LMSHandler::getForceConditions).stream().flatMap(Collection::stream));
    }
}
