package com.kotori316.limiter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.SpawnerControl;

@Mixin(BaseSpawner.class)
public abstract class SpawnerMixin {
    private int lmsDefaultSpawnCount, lmsDefaultMaxNearbyEntities; // default 0
    private int lmsCacheSpawnCount, lmsCacheMaxNearbyEntities; // default 0
    @Shadow
    private int spawnCount;
    @Shadow
    private int maxNearbyEntities;

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I"), allow = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void spawnCountHead(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (!serverLevel.isClientSide() && spawnCount == 4) {
            // 4 is the default spawn count. If not 4, the spawner is modifies by others.
            lmsDefaultSpawnCount = spawnCount;
            if (lmsCacheSpawnCount == 0) {
                serverLevel.getCapability(Caps.getLmsCapability())
                    .resolve()
                    .map(LMSHandler::getSpawnerControl)
                    .flatMap(SpawnerControl::getSpawnCount)
                    .ifPresent(newCount -> spawnCount = newCount);
                lmsCacheSpawnCount = spawnCount;
            } else {
                spawnCount = lmsCacheSpawnCount;
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I", shift = At.Shift.AFTER))
    public void spawnCountTail(CallbackInfo ci) {
        if (lmsDefaultSpawnCount == 4) {
            spawnCount = lmsDefaultSpawnCount;
            lmsDefaultSpawnCount = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;maxNearbyEntities:I"), allow = 1, locals = LocalCapture.CAPTURE_FAILHARD)
    public void maxNearbyEntitiesHead(ServerLevel serverLevel, BlockPos pos, CallbackInfo ci) {
        if (!serverLevel.isClientSide() && maxNearbyEntities == 6) {
            lmsDefaultMaxNearbyEntities = maxNearbyEntities;

            if (lmsCacheMaxNearbyEntities == 0) {
                serverLevel.getCapability(Caps.getLmsCapability())
                    .resolve()
                    .map(LMSHandler::getSpawnerControl)
                    .flatMap(SpawnerControl::getSpawnCount)
                    .ifPresent(newCount -> maxNearbyEntities = Math.max(newCount, lmsDefaultMaxNearbyEntities));
                lmsCacheMaxNearbyEntities = maxNearbyEntities;
            } else {
                maxNearbyEntities = lmsCacheMaxNearbyEntities;
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;maxNearbyEntities:I", shift = At.Shift.AFTER))
    public void maxNearbyEntitiesTail(CallbackInfo ci) {
        if (lmsDefaultMaxNearbyEntities == 6) {
            maxNearbyEntities = lmsDefaultMaxNearbyEntities;
            lmsDefaultMaxNearbyEntities = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "serverTick", at = @At("RETURN"),
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/level/BaseSpawner;spawnCount:I")))
    public void tickTail(CallbackInfo ci) {
        lmsCacheSpawnCount = 0;
        lmsCacheMaxNearbyEntities = 0;
    }
}
