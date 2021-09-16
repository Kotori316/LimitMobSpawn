package com.kotori316.limiter.command;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import javax.annotation.Nullable;
import net.minecraft.commands.SharedSuggestionProvider;

import com.kotori316.limiter.SpawnConditionLoader;

import static com.kotori316.limiter.command.TestSpawnParser.FAILED_CREATE_INSTANCE;
import static com.kotori316.limiter.command.TestSpawnParser.PROPERTY_NOT_FOUND;

class ParseCombinedCondition implements ConditionParser {
    @Override
    public void parse(String typeName,
                      StringReader reader,
                      JsonObject context,
                      Consumer<Function<SuggestionsBuilder, CompletableFuture<Suggestions>>> suggestionSetter,
                      @Nullable SharedSuggestionProvider provider)
        throws CommandSyntaxException {
        JsonArray array = new JsonArray();
        suggestionSetter.accept(TestSpawnParser::suggestRuleName);
        reader.skipWhitespace();
        while (reader.canRead() && reader.peek() != ']') {
            int first = reader.getCursor();
            // and[dimension[dim=a] dimension[dim=b]]
            //     ^ <- cursor
            // We'll find '[' and parse the condition.
            while (reader.canRead() && reader.peek() != '[')
                reader.skip();
            String typeNameInside = TestSpawnParser.getRuleName(reader, first, reader.getCursor());
            // and[dimension[dim=a] dimension[dim=b]]
            //              ^ <- cursor
            suggestionSetter.accept(TestSpawnParser::suggestStartProperties);
            JsonObject eachElement = new JsonObject();
            if (reader.canRead()) {
                reader.skip(); // Skip [
                suggestionSetter.accept(TestSpawnParser.suggestPropertyKeys(typeNameInside, eachElement));
            } else {
                throw PROPERTY_NOT_FOUND.createWithContext(reader);
            }
            // and[dimension[dim=a] dimension[dim=b]]
            //               ^ <- cursor
            ConditionParser parser = ConditionParser.findParser(typeNameInside);
            parser.parse(typeNameInside, reader, eachElement, suggestionSetter, provider);
            eachElement.addProperty("type", typeNameInside);
            array.add(eachElement);
            // and[dimension[dim=a] dimension[dim=b]]
            //                     ^ <- cursor
            suggestionSetter.accept(ConditionParser.makeSuggestion(array.size() == 1 ? Stream.of(",") : Stream.of(",", "]")));
            while (reader.canRead() && (Character.isWhitespace(reader.peek()) || reader.peek() == ',')) {
                reader.skip();
                // and[dimension[dim=a] dimension[dim=b]]
                //                      ^ <- cursor
                Stream<String> suggestions;
                if (array.size() == 1) {
                    suggestions = SpawnConditionLoader.INSTANCE.serializeKeySet().stream();
                } else {
                    suggestions = Stream.concat(SpawnConditionLoader.INSTANCE.serializeKeySet().stream(),
                        Stream.of("]"));
                }
                suggestionSetter.accept(ConditionParser.makeSuggestion(suggestions));
            }
        }
        // and[dimension[dim=a] dimension[dim=b]]
        //                                      ^ <- cursor
        if (reader.canRead() && reader.peek() == ']') {
            reader.skip();
        } else {
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, "Not finished statement.");
        }
        context.add("values", array);
    }

}
