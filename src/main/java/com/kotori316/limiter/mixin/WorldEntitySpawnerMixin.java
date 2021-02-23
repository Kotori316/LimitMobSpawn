package com.kotori316.limiter.mixin;

import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LimitMobSpawn;

@Mixin(WorldEntitySpawner.class)
public class WorldEntitySpawnerMixin {
    @Inject(method = "canCreatureTypeSpawnAtLocation", at = @At("HEAD"), cancellable = true)
    private static void canCreatureTypeSpawnAtLocation(
        EntitySpawnPlacementRegistry.PlacementType placeType, IWorldReader worldIn, BlockPos pos,
        EntityType<?> entityTypeIn, CallbackInfoReturnable<Boolean> cir) {

        LimitMobSpawn.SpawnCheckResult checkResult = LimitMobSpawn.allowSpawning(worldIn, pos, entityTypeIn, null);
        if (checkResult == LimitMobSpawn.SpawnCheckResult.DENY) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, "WorldEntitySpawnerMixin denied spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.FALSE);
        } else if (checkResult == LimitMobSpawn.SpawnCheckResult.FORCE) {
            LimitMobSpawn.LOGGER.log(LimitMobSpawn.LOG_LEVEL, "WorldEntitySpawnerMixin forced spawning of {} at {}.", entityTypeIn, pos);
            cir.setReturnValue(Boolean.TRUE);
        }
    }
}
