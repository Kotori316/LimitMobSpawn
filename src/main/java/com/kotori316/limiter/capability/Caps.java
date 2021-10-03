package com.kotori316.limiter.capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.kotori316.limiter.LimitMobSpawn;

@Mod.EventBusSubscriber(modid = LimitMobSpawn.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Caps {
    public static final Capability<LMSHandler> LMS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static Capability<LMSHandler> getLmsCapability() {
        return LMS_CAPABILITY;
    }

    @SubscribeEvent
    public static void attachEvent(AttachCapabilitiesEvent<Level> event) {
        event.addCapability(new ResourceLocation(LimitMobSpawn.MOD_ID, "world_lms_capability"),
            new LMSCapProvider(new LMSConditionsHolder()));
    }

    private static class LMSCapProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
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
        public CompoundTag serializeNBT() {
            return handler.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            handler.deserializeNBT(nbt);
        }
    }
}
