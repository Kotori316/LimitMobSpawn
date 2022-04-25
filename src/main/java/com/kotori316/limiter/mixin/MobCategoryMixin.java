package com.kotori316.limiter.mixin;

import java.util.Optional;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;

@Mixin(MobCategory.class)
public final class MobCategoryMixin {

    @Inject(method = "getMaxInstancesPerChunk", at = @At("HEAD"), cancellable = true)
    public void getMax(CallbackInfoReturnable<Integer> cir) {
        var mobNumberLimit = Optional.ofNullable(ServerLifecycleHooks.getCurrentServer())
            .map(s -> s.getLevel(Level.OVERWORLD))
            .flatMap(l -> l.getCapability(Caps.getLmsCapability()).resolve())
            .map(LMSHandler::getMobNumberLimit)
            .orElse(null);
        if (mobNumberLimit != null) {
            var count = mobNumberLimit.getLimit((MobCategory) (Object) this);
            if (count.isPresent()) {
                cir.setReturnValue(count.getAsInt());
            }
        }
    }
}
