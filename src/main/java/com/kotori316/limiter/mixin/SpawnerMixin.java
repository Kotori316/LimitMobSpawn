package com.kotori316.limiter.mixin;

import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.SpawnerControl;

@Mixin(AbstractSpawner.class)
public abstract class SpawnerMixin {
    private int lmsDefaultSpawnCount, lmsDefaultMaxNearbyEntities;
    @Shadow
    private int spawnCount;
    @Shadow
    private int maxNearbyEntities;

    @Shadow
    public abstract World shadow$getWorld();

    @Inject(method = "tick", at = @At("HEAD"), allow = 1)
    public void tickHead(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && spawnCount == 4) {
            // 4 is the default spawn count. If not 4, the spawner is modifies by others.
            lmsDefaultSpawnCount = spawnCount;
            lmsDefaultMaxNearbyEntities = maxNearbyEntities;
            shadow$getWorld().getCapability(Caps.getLmsCapability())
                .resolve()
                .map(LMSHandler::getSpawnerControl)
                .flatMap(SpawnerControl::getSpawnCount)
                .ifPresent(newCount -> {
                    spawnCount = newCount; //Math.min(newCount, defaultSpawnCount);
                    maxNearbyEntities = Math.max(spawnCount, lmsDefaultMaxNearbyEntities);
                });
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void tickTail(CallbackInfo ci) {
        if (!shadow$getWorld().isRemote() && spawnCount == 4) {
            spawnCount = lmsDefaultSpawnCount;
            maxNearbyEntities = lmsDefaultMaxNearbyEntities;
        }
    }
}
