package com.kotori316.limiter.mixin;

import java.util.Random;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.spawner.ISpecialSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LimitMobSpawn;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    /**
     * Prevent mobs from being spawned via village siege or some {@link ISpecialSpawner}.
     */
    @Inject(method = "canSpawnOn", at = @At("HEAD"), cancellable = true)
    private static void canSpawnOn(EntityType<? extends MobEntity> entityType, IWorld world, SpawnReason reason,
                                   BlockPos pos, Random randomIn, CallbackInfoReturnable<Boolean> cir) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(world, pos, entityType, reason);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, "MobEntity#canSpawnOn denied spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, "MobEntity#canSpawnOn forced spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
