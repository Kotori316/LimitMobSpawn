package com.kotori316.limiter.capability;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public interface LMSHandler extends INBTSerializable<CompoundNBT> {
    void addDefaultCondition(TestSpawn condition);

    void addDenyCondition(TestSpawn condition);

    void addForceCondition(TestSpawn condition);

    Set<TestSpawn> getDefaultConditions();

    Set<TestSpawn> getDenyConditions();

    Set<TestSpawn> getForceConditions();

    @Override
    default CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        Collector<INBT, ?, ListNBT> arrayCollector = Collector.of(ListNBT::new, ListNBT::add, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        }, Collector.Characteristics.IDENTITY_FINISH);

        nbt.put("default", getDefaultConditions().stream().map(t -> t.to(NBTDynamicOps.INSTANCE)).collect(arrayCollector));
        nbt.put("deny", getDenyConditions().stream().map(t -> t.to(NBTDynamicOps.INSTANCE)).collect(arrayCollector));
        nbt.put("force", getForceConditions().stream().map(t -> t.to(NBTDynamicOps.INSTANCE)).collect(arrayCollector));
        return nbt;
    }

    @Override
    default void deserializeNBT(CompoundNBT nbt) {
        nbt.getList("default", Constants.NBT.TAG_COMPOUND).stream()
            .map(n -> new Dynamic<>(NBTDynamicOps.INSTANCE, n))
            .map(SpawnConditionLoader.INSTANCE::deserialize)
            .forEach(this::addDefaultCondition);
        nbt.getList("deny", Constants.NBT.TAG_COMPOUND).stream()
            .map(n -> new Dynamic<>(NBTDynamicOps.INSTANCE, n))
            .map(SpawnConditionLoader.INSTANCE::deserialize)
            .forEach(this::addDenyCondition);
        nbt.getList("force", Constants.NBT.TAG_COMPOUND).stream()
            .map(n -> new Dynamic<>(NBTDynamicOps.INSTANCE, n))
            .map(SpawnConditionLoader.INSTANCE::deserialize)
            .forEach(this::addForceCondition);
    }

    static void registerCapability() {
        LMSCapability cap = new LMSCapability();
        CapabilityManager.INSTANCE.register(LMSHandler.class, cap, cap);
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

final class LMSCapability implements Capability.IStorage<LMSHandler>, Callable<LMSHandler> {

    @Override
    public LMSHandler call() {
        return new LMSConditionsHolder();
    }

    @Override
    public INBT writeNBT(Capability<LMSHandler> capability, LMSHandler instance, Direction side) {
        return instance.serializeNBT();
    }

    @Override
    public void readNBT(Capability<LMSHandler> capability, LMSHandler instance, Direction side, INBT nbt) {
        instance.deserializeNBT((CompoundNBT) nbt);
    }
}
