package com.kotori316.limiter.command;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.text.StringTextComponent;

import com.kotori316.limiter.LimitMobSpawn;
import com.kotori316.limiter.SpawnConditionLoader;
import com.kotori316.limiter.TestSpawn;

public class TestSpawnArgument implements ArgumentType<TestSpawn> {
    public static void registerArgumentType() {
        ArgumentTypes.register(LimitMobSpawn.MOD_ID + ":rule", TestSpawnArgument.class, new ArgumentSerializer<>(TestSpawnArgument::new));
    }

    @Override
    public TestSpawn parse(StringReader reader) throws CommandSyntaxException {
        TestSpawnParser parser = new TestSpawnParser(reader);
        parser.parse();
        return parser.createInstance();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringreader = new StringReader(builder.getInput());
        stringreader.setCursor(builder.getStart());
        TestSpawnParser parser = new TestSpawnParser(stringreader);
        try {
            parser.parse();
        } catch (CommandSyntaxException ignore) {
        }
        return parser.getSuggestion(builder);
    }
}

class TestSpawnParser {
    private static final SimpleCommandExceptionType TYPE_NOT_FOUND = new SimpleCommandExceptionType(new StringTextComponent("Type not found."));
    private static final SimpleCommandExceptionType PROPERTY_NOT_FOUND = new SimpleCommandExceptionType(new StringTextComponent("Property not found."));
    private static final DynamicCommandExceptionType FAILED_CREATE_INSTANCE = new DynamicCommandExceptionType(o -> new StringTextComponent("Error " + o));
    private final StringReader reader;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestion = SuggestionsBuilder::buildFuture;
    private String ruleName;
    private final JsonObject object = new JsonObject();

    TestSpawnParser(StringReader reader) {
        this.reader = reader;
    }

    // Step 1: Get rule name
    String getRuleName(int first, int endExclusive) throws CommandSyntaxException {
        String name = reader.getString().substring(first, endExclusive);
        if (SpawnConditionLoader.INSTANCE.hasSerializeKey(name)) {
            return name;
        } else {
            reader.setCursor(first);
            throw TYPE_NOT_FOUND.createWithContext(reader);
        }
    }

    private CompletableFuture<Suggestions> suggestRuleName(SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(SpawnConditionLoader.INSTANCE.serializeKeySet(), builder);
    }

    private CompletableFuture<Suggestions> suggestStartProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf('['));
        }
        return builder.buildFuture();
    }

    // Step 2: Get rule properties
    void readRuleProperties(int first, int endExclusive) throws CommandSyntaxException {
        String pair = reader.getString().substring(first, endExclusive);
        if (!pair.contains("=")) {
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, "= expected");
        }
        try {
            String[] split = pair.split("=", 2);
            try {
                object.addProperty(split[0], Integer.parseInt(split[1]));
            } catch (NumberFormatException ignore) {
                object.addProperty(split[0], split[1]);
            }
        } catch (RuntimeException e) {
            reader.setCursor(first);
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, e);
        }
    }

    private CompletableFuture<Suggestions> suggestEndProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(']'));
        }
        return builder.buildFuture();
    }

    void parse() throws CommandSyntaxException {
        {
            // Step 1
            this.suggestion = this::suggestRuleName;
            int i = reader.getCursor();
            while (reader.canRead() && reader.peek() != '[') {
                reader.skip();
            }
            ruleName = getRuleName(i, reader.getCursor());
            reader.skipWhitespace();
            this.suggestion = this::suggestStartProperties;
        }
        if (reader.canRead()) {
            reader.skip(); // Skip [
            this.suggestion = SuggestionsBuilder::buildFuture;
        } else {
            throw PROPERTY_NOT_FOUND.createWithContext(reader);
        }
        {
            // Step 2
            while (reader.canRead() && reader.peek() != ']') {
                reader.skipWhitespace();
                int pairStart = reader.getCursor();
                while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                    reader.skip();
                }
                readRuleProperties(pairStart, reader.getCursor());
                if (reader.canRead()) {
                    if (reader.peek() == ',') reader.skip(); // Skip ,
                    else this.suggestion = SuggestionsBuilder::buildFuture;
                } else {
                    this.suggestion = this::suggestEndProperties;
                }
            }
            if (reader.canRead() && reader.peek() == ']') {
                reader.skip();
            } else {
                throw FAILED_CREATE_INSTANCE.createWithContext(reader, "Not finished statement.");
            }
        }
    }

    TestSpawn createInstance() throws CommandSyntaxException {
        object.addProperty("type", this.ruleName);
        try {
            return SpawnConditionLoader.INSTANCE.deserialize(object);
        } catch (RuntimeException e) {
            throw FAILED_CREATE_INSTANCE.createWithContext(this.reader, e);
        }
    }

    CompletableFuture<Suggestions> getSuggestion(SuggestionsBuilder builder) {
        return this.suggestion.apply(builder.createOffset(this.reader.getCursor()));
    }

    @VisibleForTesting
    String foundRuleName() {
        return ruleName;
    }
}
