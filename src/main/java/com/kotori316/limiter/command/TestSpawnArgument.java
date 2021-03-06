package com.kotori316.limiter.command;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.JSONUtils;
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
            parser.parseWithProvider((ISuggestionProvider) context.getSource());
        } catch (CommandSyntaxException ignore) {
        }
        return parser.getSuggestion(builder);
    }
}

class TestSpawnParser {
    static final SimpleCommandExceptionType TYPE_NOT_FOUND = new SimpleCommandExceptionType(new StringTextComponent("Type not found."));
    static final SimpleCommandExceptionType PROPERTY_NOT_FOUND = new SimpleCommandExceptionType(new StringTextComponent("Property not found."));
    static final DynamicCommandExceptionType FAILED_CREATE_INSTANCE = new DynamicCommandExceptionType(o -> new StringTextComponent("Error " + o));
    private final StringReader reader;
    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestion = SuggestionsBuilder::buildFuture;
    private String ruleName;
    private final JsonObject object = new JsonObject();

    TestSpawnParser(StringReader reader) {
        this.reader = reader;
    }

    // Step 1: Get rule name
    static String getRuleName(StringReader reader, int first, int endExclusive) throws CommandSyntaxException {
        String name = reader.getString().substring(first, endExclusive);
        if (SpawnConditionLoader.INSTANCE.hasSerializeKey(name)) {
            return name;
        } else {
            reader.setCursor(first);
            throw TYPE_NOT_FOUND.createWithContext(reader);
        }
    }

    static CompletableFuture<Suggestions> suggestRuleName(SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(SpawnConditionLoader.INSTANCE.serializeKeySet(), builder);
    }

    static CompletableFuture<Suggestions> suggestStartProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf('['));
        }
        return builder.buildFuture();
    }

    static Set<String> getPropertyKeysRest(String ruleName, JsonObject object) {
        return SpawnConditionLoader.INSTANCE.getSerializer(ruleName).propertyKeys()
            .stream().filter(aKey -> !JSONUtils.hasField(object, aKey)).collect(Collectors.toSet());
    }

    static Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestPropertyKeys(String ruleName, JsonObject object) {
        return (SuggestionsBuilder builder) -> ISuggestionProvider.suggest(getPropertyKeysRest(ruleName, object), builder);
    }

    static CompletableFuture<Suggestions> suggestComma(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(','));
        }
        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestEndProperties(SuggestionsBuilder builder) {
        if (builder.getRemaining().isEmpty()) {
            builder.suggest(String.valueOf(']'));
        }
        return builder.buildFuture();
    }

    void parse() throws CommandSyntaxException {
        parseWithProvider(null);
    }

    void parseWithProvider(@Nullable ISuggestionProvider provider) throws CommandSyntaxException {
        {
            // Step 1
            this.suggestion = TestSpawnParser::suggestRuleName;
            int i = reader.getCursor();
            while (reader.canRead() && reader.peek() != '[') {
                reader.skip();
            }
            ruleName = getRuleName(reader, i, reader.getCursor());
            reader.skipWhitespace();
            this.suggestion = TestSpawnParser::suggestStartProperties;
        }
        if (reader.canRead()) {
            reader.skip(); // Skip [
            this.suggestion = suggestPropertyKeys(ruleName, object);
        } else {
            throw PROPERTY_NOT_FOUND.createWithContext(reader);
        }

        ConditionParser parser = ConditionParser.findParser(ruleName);
        parser.parse(ruleName, reader, object, this::setSuggestion, provider);
    }

    private void setSuggestion(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestion) {
        this.suggestion = suggestion;
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
