package com.kotori316.limiter.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.kotori316.limiter.LimitMobSpawn;

@Mod.EventBusSubscriber(modid = LimitMobSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Caps {
    @CapabilityInject(LMSHandler.class)
    public static final Capability<LMSHandler> LMS_CAPABILITY = null;

    public static Capability<LMSHandler> getLmsCapability() {
        return LMS_CAPABILITY;
    }

    @SubscribeEvent
    public static void attachEvent(AttachCapabilitiesEvent<World> event) {
        event.addCapability(new ResourceLocation(LimitMobSpawn.MOD_ID, "world_lms_capability"),
            new LMSCapProvider(new LMSConditionsHolder()));
    }

    public static class LMSCapProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {
        private final LMSHandler handler;
        private final LazyOptional<LMSHandler> optional;

        public LMSCapProvider(LMSHandler handler) {
            this.handler = handler;
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return Caps.getLmsCapability().orEmpty(cap, optional);
        }

        @Override
        public CompoundNBT serializeNBT() {
            return handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            handler.deserializeNBT(nbt);
        }
    }
}
