package com.kotori316.limiter.mixin;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.SpawnerControl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BaseSpawner.class)
public abstract class SpawnerMixin {
    @Unique
    private int lms_DefaultSpawnCount, lms_DefaultMaxNearbyEntities; // default 0
    @Unique
    private int lms_CacheSpawnCount, lms_CacheMaxNearbyEntities; // default 0
    @Shadow
    private int spawnCount;
    @Shadow
    private int maxNearbyEntities;

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I"), allow = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void spawnCountHead(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (!serverLevel.isClientSide() && spawnCount == 4) {
            // 4 is the default spawn count. If not 4, the spawner is modified by others.
            lms_DefaultSpawnCount = spawnCount;
            if (lms_CacheSpawnCount == 0) {
                serverLevel.getCapability(Caps.getLmsCapability())
                    .resolve()
                    .map(LMSHandler::getSpawnerControl)
                    .flatMap(SpawnerControl::getSpawnCount)
                    .ifPresent(newCount -> spawnCount = newCount);
                lms_CacheSpawnCount = spawnCount;
            } else {
                spawnCount = lms_CacheSpawnCount;
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I", shift = At.Shift.AFTER))
    public void spawnCountTail(CallbackInfo ci) {
        if (lms_DefaultSpawnCount == 4) {
            spawnCount = lms_DefaultSpawnCount;
            lms_DefaultSpawnCount = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;maxNearbyEntities:I"), allow = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void maxNearbyEntitiesHead(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (!serverLevel.isClientSide() && maxNearbyEntities == 6) {
            lms_DefaultMaxNearbyEntities = maxNearbyEntities;

            if (lms_CacheMaxNearbyEntities == 0) {
                serverLevel.getCapability(Caps.getLmsCapability())
                    .resolve()
                    .map(LMSHandler::getSpawnerControl)
                    .flatMap(SpawnerControl::getSpawnCount)
                    .ifPresent(newCount -> maxNearbyEntities = Math.max(newCount, lms_DefaultMaxNearbyEntities));
                lms_CacheMaxNearbyEntities = maxNearbyEntities;
            } else {
                maxNearbyEntities = lms_CacheMaxNearbyEntities;
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;maxNearbyEntities:I", shift = At.Shift.AFTER))
    public void maxNearbyEntitiesTail(CallbackInfo ci) {
        if (lms_DefaultMaxNearbyEntities == 6) {
            maxNearbyEntities = lms_DefaultMaxNearbyEntities;
            lms_DefaultMaxNearbyEntities = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At("RETURN"),
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I")))
    public void tickTail(CallbackInfo ci) {
        lms_CacheSpawnCount = 0;
        lms_CacheMaxNearbyEntities = 0;
    }
}
