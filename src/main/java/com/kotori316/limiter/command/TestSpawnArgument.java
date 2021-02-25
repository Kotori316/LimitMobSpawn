package com.kotori316.limiter.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.conditions.All;

public class TestSpawnArgument implements ArgumentType<TestSpawn> {
    public static void registerArgumentType() {
        ArgumentTypes.register(LimitMobSpawn.MOD_ID + ":rule", TestSpawnArgument.class, new ArgumentSerializer<>(TestSpawnArgument::new));
    }

    @Override
    public TestSpawn parse(StringReader reader) throws CommandSyntaxException {
        return All.getInstance();
    }
}
