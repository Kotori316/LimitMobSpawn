package com.kotori316.limiter.command;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import javax.annotation.Nullable;
import net.minecraft.command.ISuggestionProvider;

import com.kotori316.limiter.SpawnConditionLoader;

import static com.kotori316.limiter.command.TestSpawnParser.FAILED_CREATE_INSTANCE;
import static com.kotori316.limiter.command.TestSpawnParser.PROPERTY_NOT_FOUND;

class ParseNotCondition implements ConditionParser {
    @Override
    public void parse(String typeName,
                      StringReader reader,
                      JsonObject context,
                      Consumer<Function<SuggestionsBuilder, CompletableFuture<Suggestions>>> suggestionSetter,
                      @Nullable ISuggestionProvider provider)
        throws CommandSyntaxException {
        suggestionSetter.accept(ConditionParser.makeSuggestion(
            SpawnConditionLoader.INSTANCE.serializeKeySet().stream()
                .filter(Predicate.isEqual("not").negate())
        ));
        reader.skipWhitespace();

        int first = reader.getCursor();
        // not[dimension[dim=a]]
        //     ^ <- cursor
        // We'll find '[' and parse the condition.
        while (reader.canRead() && reader.peek() != '[')
            reader.skip();
        String typeNameInside = TestSpawnParser.getRuleName(reader, first, reader.getCursor());
        // not[dimension[dim=a]]
        //              ^ <- cursor
        JsonObject eachElement = new JsonObject();
        if (reader.canRead()) {
            reader.skip(); // Skip [
            suggestionSetter.accept(TestSpawnParser.suggestPropertyKeys(typeNameInside, eachElement));
        } else {
            throw PROPERTY_NOT_FOUND.createWithContext(reader);
        }
        // not[dimension[dim=a]]
        //               ^ <- cursor
        ConditionParser parser = ConditionParser.findParser(typeNameInside);
        parser.parse(typeNameInside, reader, eachElement, suggestionSetter, provider);
        eachElement.addProperty("type", typeNameInside);
        reader.skipWhitespace();
        // not[dimension[dim=a]]
        //                     ^ <- cursor
        suggestionSetter.accept(TestSpawnParser::suggestEndProperties);
        if (reader.canRead() && reader.peek() == ']') {
            reader.skip();
        } else {
            throw FAILED_CREATE_INSTANCE.createWithContext(reader, "Not finished statement.");
        }
        context.add("value", eachElement);
    }
}
