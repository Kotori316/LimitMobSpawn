package com.kotori316.limiter.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import javax.annotation.Nonnull;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import com.kotori316.limiter.Config;
import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.capability.Caps;
import com.kotori316.limiter.capability.LMSConditionsHolder;
import com.kotori316.limiter.capability.LMSHandler;
import com.kotori316.limiter.capability.RuleType;

public class LMSCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> literal = Commands.literal(LimitMobSpawn.MOD_ID);
        {
            // query
            LiteralArgumentBuilder<CommandSource> query = Commands.literal("query");

            LiteralArgumentBuilder<CommandSource> world = Commands.literal("world");
            registerQuery(world, LMSCommand::getLmsHandler);
            query.then(world);
            LiteralArgumentBuilder<CommandSource> dataPack = Commands.literal("datapack");
            registerQuery(dataPack, c -> SpawnConditionLoader.INSTANCE.getHolder());
            query.then(dataPack);

            literal.then(query
                .executes(context -> {
                    LMSHandler lmsHandler = getLmsHandler(context);
                    if (lmsHandler.getDefaultConditions().isEmpty() && lmsHandler.getForceConditions().isEmpty() && lmsHandler.getDenyConditions().isEmpty())
                        context.getSource().sendErrorMessage(new StringTextComponent("No Rules found."));
                    sendMessage(context, "Defaults", Sets.union(lmsHandler.getDefaultConditions(), SpawnConditionLoader.INSTANCE.getHolder().getDefaultConditions()));
                    sendMessage(context, "Denies", Sets.union(lmsHandler.getDenyConditions(), SpawnConditionLoader.INSTANCE.getHolder().getDenyConditions()));
                    sendMessage(context, "Forces", Sets.union(lmsHandler.getForceConditions(), SpawnConditionLoader.INSTANCE.getHolder().getForceConditions()));
                    return Command.SINGLE_SUCCESS;
                }));
        }
        {
            // add
            LiteralArgumentBuilder<CommandSource> add = Commands.literal("add").requires(s -> s.hasPermissionLevel(Config.getInstance().getPermission()));
            for (RuleType ruleType : RuleType.values()) {
                add.then(Commands.literal(ruleType.saveName()).then(Commands.argument("rule", new TestSpawnArgument()).executes(context -> {
                    List<LMSHandler> list = getAllLmsHandlers(context);
                    TestSpawn rule = context.getArgument("rule", TestSpawn.class);
                    list.forEach(lmsHandler -> ruleType.add(lmsHandler, rule));
                    context.getSource().sendFeedback(new StringTextComponent(String.format("Added %s to %s.", rule, ruleType.saveName())), true);
                    return Command.SINGLE_SUCCESS;
                })));
            }
            literal.then(add);
        }
        {
            // remove
            LiteralArgumentBuilder<CommandSource> remove = Commands.literal("remove").requires(s -> s.hasPermissionLevel(Config.getInstance().getPermission()));
            for (RuleType ruleType : RuleType.values()) {
                remove.then(Commands.literal(ruleType.saveName()).executes(context -> {
                    getAllLmsHandlers(context).forEach(ruleType::removeAll);
                    context.getSource().sendFeedback(new StringTextComponent("Cleared " + ruleType.getCommandName()), true);
                    return Command.SINGLE_SUCCESS;
                }));
            }
            literal.then(remove);
        }
        dispatcher.register(literal);
    }

    private static void registerQuery(LiteralArgumentBuilder<CommandSource> parent, Function<CommandContext<CommandSource>, LMSHandler> getter) {
        for (RuleType ruleType : RuleType.values()) {
            parent.then(Commands.literal(ruleType.saveName()).executes(context -> {
                LMSHandler lmsHandler = getter.apply(context);
                sendMessage(context, ruleType.getCommandName(), ruleType.getRules(lmsHandler));
                return Command.SINGLE_SUCCESS;
            }));
        }
        parent.executes(context -> {
            LMSHandler lmsHandler = getter.apply(context);
            for (RuleType ruleType : RuleType.values()) {
                sendMessage(context, ruleType.getCommandName(), ruleType.getRules(lmsHandler));
            }
            return Command.SINGLE_SUCCESS;
        });
    }

    private static void sendMessage(CommandContext<CommandSource> context, String s, Set<TestSpawn> conditions) {
        context.getSource().sendFeedback(new StringTextComponent(s + "=" + conditions.size()), true);
        conditions.stream().map(Object::toString).map(StringTextComponent::new).forEach(c -> context.getSource().sendFeedback(c, true));
    }

    @Nonnull
    private static LMSHandler getLmsHandler(CommandContext<CommandSource> context) {
        World world = context.getSource().getWorld();
        return world.getCapability(Caps.getLmsCapability()).orElseGet(LMSConditionsHolder::new);
    }

    private static List<LMSHandler> getAllLmsHandlers(CommandContext<CommandSource> context) {
        List<LMSHandler> list = new ArrayList<>();
        for (World world : context.getSource().getServer().getWorlds()) {
            world.getCapability(Caps.getLmsCapability()).ifPresent(list::add);
        }
        return list;
    }
}
