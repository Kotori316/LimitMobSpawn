package com.kotori316.limiter.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LMSEventHandler;
import com.kotori316.limiter.LimitMobSpawn;

@SuppressWarnings("DuplicatedCode")
@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {
    /**
     * Prevent mobs from being spawned via natural spawning.
     */
    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void isSpawnPositionOk(
        SpawnPlacements.Type placeType, LevelReader worldIn, BlockPos pos,
        EntityType<?> entityTypeIn, CallbackInfoReturnable<Boolean> cir
    ) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(worldIn, pos, entityTypeIn, null);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "NaturalSpawner#isSpawnPositionOk denied spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "NaturalSpawner#isSpawnPositionOk forced spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    /**
     * Prevent mobs from being spawned via some {@link CustomSpawner}.
     */
    @Inject(method = "isValidEmptySpawnBlock", at = @At("HEAD"), cancellable = true)
    private static void isValidEmptySpawnBlock(
        BlockGetter worldIn, BlockPos pos, BlockState state, FluidState fluidState, EntityType<?> entityTypeIn, CallbackInfoReturnable<Boolean> cir
    ) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(worldIn, pos, entityTypeIn, null);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "NaturalSpawner#isValidEmptySpawnBlock denied spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "NaturalSpawner#isValidEmptySpawnBlock forced spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
