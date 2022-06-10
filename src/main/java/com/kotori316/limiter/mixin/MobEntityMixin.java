package com.kotori316.limiter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LMSEventHandler;
import com.kotori316.limiter.LimitMobSpawn;

@Mixin(Mob.class)
public class MobEntityMixin {
    /**
     * Prevent mobs from being spawned via village siege or some {@link CustomSpawner}.
     */
    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void checkMobSpawnRules(EntityType<? extends Mob> entityType, LevelAccessor world, MobSpawnType reason,
                                           BlockPos pos, RandomSource randomIn, CallbackInfoReturnable<Boolean> cir) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(world, pos, entityType, reason);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER, "MobEntity#checkMobSpawnRules denied spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER, "MobEntity#checkMobSpawnRules forced spawning of {} by {} at {}.", entityType, reason, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
