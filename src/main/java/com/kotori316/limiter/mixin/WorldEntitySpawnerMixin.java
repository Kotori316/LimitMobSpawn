package com.kotori316.limiter.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.spawner.ISpecialSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LMSEventHandler;
import com.kotori316.limiter.LimitMobSpawn;

@SuppressWarnings("DuplicatedCode")
@Mixin(WorldEntitySpawner.class)
public class WorldEntitySpawnerMixin {
    /**
     * Prevent mobs from being spawned via natural spawning.
     */
    @Inject(method = "canCreatureTypeSpawnAtLocation", at = @At("HEAD"), cancellable = true)
    private static void canCreatureTypeSpawnAtLocation(
        EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos,
        EntityType<?> entityTypeIn, CallbackInfoReturnable<Boolean> cir
    ) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(worldIn, pos, entityTypeIn, null);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "WorldEntitySpawner#canCreatureTypeSpawnAtLocation denied spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "WorldEntitySpawner#canCreatureTypeSpawnAtLocation forced spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }

    /**
     * Prevent mobs from being spawned via some {@link ISpecialSpawner}.
     */
    @Inject(method = "func_234968_a_", at = @At("HEAD"), cancellable = true)
    private static void func_234968_a_(
        IBlockReader worldIn, BlockPos pos, BlockState state, FluidState fluidState, EntityType<?> entityTypeIn, CallbackInfoReturnable<Boolean> cir
    ) {
        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(worldIn, pos, entityTypeIn, null);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "WorldEntitySpawner#func_234968_a_ denied spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, LMSEventHandler.LMS_MARKER,
                "WorldEntitySpawner#func_234968_a_ forced spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
