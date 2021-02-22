package com.kotori316.limiter.mixin;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LimitMobSpawn;

@Mixin(EntitySpawnPlacementRegistry.class)
public class EntitySpawnPlacementRegistryMixin {
    @Inject(method = "canSpawnEntity", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void canSpawnEntity(EntityType<T> entityType, IServerWorld world, SpawnReason reason,
                                                          BlockPos pos, Random rand, CallbackInfoReturnable<Boolean> cir) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(world, pos, entityType, reason);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY)
            cir.setReturnValue(Boolean.FALSE);
        else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE)
            cir.setReturnValue(Boolean.TRUE);
    }
}
