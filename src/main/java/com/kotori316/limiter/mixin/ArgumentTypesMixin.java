package com.kotori316.limiter.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.command.TestSpawnArgument;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypesMixin {
    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T>
    register(Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentTypeInfo<A, T> argumentSerializer) {
        throw new AssertionError("Nope.");
    }

    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void registerTestSpawnArgument(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> ci) {
        register(registry, LimitMobSpawn.MOD_ID + ":rule", TestSpawnArgument.class, SingletonArgumentInfo.contextFree(TestSpawnArgument::new));
    }
}
