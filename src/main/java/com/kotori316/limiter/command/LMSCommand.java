package com.kotori316.limiter.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSHandler;

public class LMSCommand {
    private static final SimpleCommandExceptionType HandlersNotFound = new SimpleCommandExceptionType(new StringTextComponent("No LMS Handlers found."));
    private static final SimpleCommandExceptionType RulesNotFound = new SimpleCommandExceptionType(new StringTextComponent("No Rules found."));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literal = Commands.literal(LimitMobSpawn.MOD_ID);
        {
            // query
            literal.then(Commands.literal("query").executes(context -> {
                World world = context.getSource().getWorld();
                LMSHandler lmsHandler = world.getCapability(Caps.getLmsCapability()).orElseThrow(HandlersNotFound::create);
                if (lmsHandler.getDefaultConditions().isEmpty() && lmsHandler.getForceConditions().isEmpty() && lmsHandler.getDenyConditions().isEmpty())
                    throw RulesNotFound.create();
                context.getSource().sendFeedback(new StringTextComponent("Defaults"), true);
                lmsHandler.getDefaultConditions().stream().map(Object::toString).map(StringTextComponent::new).forEach(c -> context.getSource().sendFeedback(c, true));
                context.getSource().sendFeedback(new StringTextComponent("Denies"), true);
                lmsHandler.getDenyConditions().stream().map(Object::toString).map(StringTextComponent::new).forEach(c -> context.getSource().sendFeedback(c, true));
                context.getSource().sendFeedback(new StringTextComponent("Forces"), true);
                lmsHandler.getForceConditions().stream().map(Object::toString).map(StringTextComponent::new).forEach(c -> context.getSource().sendFeedback(c, true));
                return Command.SINGLE_SUCCESS;
            }));
        }
        dispatcher.register(literal);
    }
}
