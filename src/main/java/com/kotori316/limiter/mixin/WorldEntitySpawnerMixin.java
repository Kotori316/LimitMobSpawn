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

        if (!LimitMobSpawn.allowSpawning(placeType, worldIn, pos, entityTypeIn))
            cir.setReturnValue(Boolean.FALSE);
    }
}
