package com.kotori316.limiter.mixin;

import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.SpawnerControl;

@Mixin(AbstractSpawner.class)
public abstract class SpawnerMixin {
    private int lmsDefaultSpawnCount, lmsDefaultMaxNearbyEntities; // default 0
    private int lmsCacheSpawnCount, lmsCacheMaxNearbyEntities; // default 0
    @Shadow
    private int spawnCount;
    @Shadow
    private int maxNearbyEntities;

    @Shadow
    public abstract World shadow$getWorld();

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/spawner/AbstractSpawner;spawnCount:I"), allow = 1)
    public void spawnCountHead(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && spawnCount == 4) {
            // 4 is the default spawn count. If not 4, the spawner is modifies by others.
            lmsDefaultSpawnCount = spawnCount;
            if (lmsCacheSpawnCount == 0) {
                shadow$getWorld().getCapability(Caps.getLmsCapability())
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
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/spawner/AbstractSpawner;spawnCount:I", shift = At.Shift.AFTER))
    public void spawnCountTail(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && lmsDefaultSpawnCount == 4) {
            spawnCount = lmsDefaultSpawnCount;
            lmsDefaultSpawnCount = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/spawner/AbstractSpawner;maxNearbyEntities:I"), allow = 1)
    public void maxNearbyEntitiesHead(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && maxNearbyEntities == 6) {
            lmsDefaultMaxNearbyEntities = maxNearbyEntities;

            if (lmsCacheMaxNearbyEntities == 0) {
                shadow$getWorld().getCapability(Caps.getLmsCapability())
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
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/spawner/AbstractSpawner;maxNearbyEntities:I", shift = At.Shift.AFTER))
    public void maxNearbyEntitiesTail(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && lmsDefaultMaxNearbyEntities == 6) {
            maxNearbyEntities = lmsDefaultMaxNearbyEntities;
            lmsDefaultMaxNearbyEntities = 0;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Inject(method = "tick", at = @At("RETURN"),
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/spawner/AbstractSpawner;spawnCount:I")))
    public void tickTail(CallbackInfo ci) {
        lmsCacheSpawnCount = 0;
        lmsCacheMaxNearbyEntities = 0;
    }
}
