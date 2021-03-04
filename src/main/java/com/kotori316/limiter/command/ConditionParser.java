package com.kotori316.limiter.command;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

interface ConditionParser {
    /**
     * Parse the argument in reader and push read data into the context.
     *
     * @param typeName the name of type.
     * @param reader   command argument. If parsing finished, the cursor should be the end of one condition.
     *                 If failed, the cursor should be the first.
     * @param context  the holder of data
     * @throws CommandSyntaxException command parse failed.
     */
    void parse(String typeName, StringReader reader, JsonObject context, Consumer<Function<SuggestionsBuilder, CompletableFuture<Suggestions>>> suggestionSetter)
        throws CommandSyntaxException;

    ConditionParser ONE_CONDITION = new ParseOneCondition();
    ConditionParser COMBINED = new ParseCombinedCondition();
    ConditionParser NOT_CONDITION = new ParseNotCondition();

    static ConditionParser findParser(String typeName) {
        if ("and".equals(typeName) || "or".equals(typeName))
            return COMBINED;
        if ("not".equals(typeName))
            return NOT_CONDITION;
        return ONE_CONDITION;
    }
}
