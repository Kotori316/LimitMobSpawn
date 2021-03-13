package com.kotori316.limiter.command;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;

import com.kotori316.limiter.SpawnConditionLoader;

import static com.kotori316.limiter.command.TestSpawnParser.FAILED_CREATE_INSTANCE;

class ParseOneCondition implements ConditionParser {

    @Override
    public void parse(String typeName, StringReader reader, JsonObject context, Consumer<Function<SuggestionsBuilder, CompletableFuture<Suggestions>>> suggestionSetter)
        throws CommandSyntaxException {
        // Step 2
        while (reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace();
            int pairStart = reader.getCursor();
            while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                reader.skip();
            }
            readRuleProperties(typeName, reader, pairStart, reader.getCursor(), context, suggestionSetter);
            if (reader.canRead()) {
                if (reader.peek() == ',') {
                    reader.skip(); // Skip ,
                    suggestionSetter.accept(TestSpawnParser.suggestPropertyKeys(typeName, context));
                } else {
                    suggestionSetter.accept(SuggestionsBuilder::buildFuture);
                }
            }
        }
        if (reader.canRead() && reader.peek() == ']') {
            reader.skip();
        } else {
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, "Not finished statement.");
        }
    }

    // Step 2: Get rule properties
    void readRuleProperties(String ruleName, StringReader reader, int first, int endExclusive, JsonObject object,
                            Consumer<Function<SuggestionsBuilder, CompletableFuture<Suggestions>>> suggestionSetter) throws CommandSyntaxException {
        String pair = reader.getString().substring(first, endExclusive);
        if (!pair.contains("=")) {
            reader.setCursor(first);
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, "= expected after key");
        } else {
            suggestionSetter.accept(generateSuggestPropertyValues(ruleName, pair.substring(0, pair.indexOf("="))));
        }
        try {
            String[] split = pair.split("=", 2);
            try {
                object.addProperty(split[0], Integer.parseInt(split[1]));
            } catch (NumberFormatException ignore) {
                Set<String> possibleValues = SpawnConditionLoader.INSTANCE.getSerializer(ruleName).possibleValues(split[0], false);
                if (possibleValues.isEmpty() || possibleValues.contains(split[1])) {
                    object.addProperty(split[0], split[1]);
                } else {
                    reader.setCursor(first + pair.indexOf("=") + 1);
                    throw FAILED_CREATE_INSTANCE.createWithContext(reader, "invalid value");
                }
            }
            if (TestSpawnParser.getPropertyKeysRest(ruleName, object).isEmpty()) {
                // Added the last property, we expect ']'
                suggestionSetter.accept(TestSpawnParser::suggestEndProperties);
            } else {
                if (split[1].isEmpty()) {
                    // Maybe int input and int is empty, then we expect numbers, but suggest nothing.
                    suggestionSetter.accept(SuggestionsBuilder::buildFuture);
                } else {
                    // User needs add more property. We expect ','
                    suggestionSetter.accept(TestSpawnParser::suggestComma);
                }
            }
        } catch (RuntimeException e) {
            reader.setCursor(first);
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, e);
        }
    }

    private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> generateSuggestPropertyValues(String ruleName, String key) {
        return builder -> ISuggestionProvider.suggest(
            SpawnConditionLoader.INSTANCE.getSerializer(ruleName).possibleValues(key, true),
            builder);
    }

}