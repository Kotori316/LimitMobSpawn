package com.kotori316.limiter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LMSEventHandler;
import com.kotori316.limiter.LimitMobSpawn;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {
    /**
     * Prevent mobs from being spawned via monster spawner.
     */
    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void checkSpawnRules(EntityType<T> entityType, ServerLevelAccessor world, MobSpawnType reason,
                                                           BlockPos pos, RandomSource rand, CallbackInfoReturnable<Boolean> cir) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(world, pos, entityType, reason);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            if (reason != MobSpawnType.NATURAL)
                LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                    "SpawnPlacements#checkSpawnRules denied spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            if (reason != MobSpawnType.NATURAL)
                LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                    "SpawnPlacements#checkSpawnRules forced spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
