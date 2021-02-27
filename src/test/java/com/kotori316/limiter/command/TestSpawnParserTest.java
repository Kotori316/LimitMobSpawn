package com.kotori316.limiter.command;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.PositionLimit;
import com.kotori316.limiter.conditions.SpawnReasonLimit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestSpawnParserTest extends BeforeAllTest {
    @Test
    void createInstance() {
        TestSpawnParser parser = new TestSpawnParser(new StringReader(""));
        assertNotNull(parser);
    }

    @Test
    @DisplayName("All type serialization")
    void findType() throws ExecutionException, InterruptedException {
        TestSpawnParser parser = new TestSpawnParser(new StringReader("all"));
        assertThrows(CommandSyntaxException.class, parser::parse);
        assertEquals("all", parser.foundRuleName());
        Suggestions suggestions = parser.getSuggestion(new SuggestionsBuilder("all", 0)).get();
        assertEquals("[", suggestions.getList().get(0).getText());
    }

    @Test
    void findType2() {
        TestSpawnParser parser = new TestSpawnParser(new StringReader("al"));
        assertThrows(CommandSyntaxException.class, parser::parse);
        assertNull(parser.foundRuleName());
    }

    @Test
    void suggestionType() throws ExecutionException, InterruptedException {
        String input = "";
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertThrows(CommandSyntaxException.class, parser::parse);
        Suggestions suggestions = parser.getSuggestion(new SuggestionsBuilder(input, 0)).get();
        assertAll(Stream.of("all", "classification", "position").map(
            s -> () -> assertTrue(suggestions.getList().stream().anyMatch(su -> su.getText().equals(s)), "Suggestion of " + s + " whole:" + suggestions)
        ));
        assertFalse(suggestions.getList().stream().map(Suggestion::getText).anyMatch(Predicate.isEqual("anonymous")));
    }

    @Test
    void createAllType() throws CommandSyntaxException {
        String input = "all[]";
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        TestSpawn testSpawn = parser.createInstance();
        assertEquals(All.getInstance(), testSpawn);
    }

    @ParameterizedTest
    @ValueSource(strings = {"NATURAL", "natural", "event", "SPAWNER"})
    void createSpawnType(String name) throws CommandSyntaxException {
        String input = String.format("spawn_reason[spawn_reason=%s]", name);
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        SpawnReasonLimit limit = (SpawnReasonLimit) parser.createInstance();
        assertEquals(SpawnReason.valueOf(name.toUpperCase(Locale.ROOT)), limit.getReason());
    }

    @Test
    void suggestSpawnType1() throws ExecutionException, InterruptedException {
        String input = "spawn_reason[";
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertThrows(CommandSyntaxException.class, parser::parse);
        Suggestions suggestions = parser.getSuggestion(new SuggestionsBuilder(input, 0)).get();
        assertEquals(Collections.singletonList("spawn_reason"), suggestions.getList().stream().map(Suggestion::getText).collect(Collectors.toList()));
    }

    @Test
    void createPositionType() throws CommandSyntaxException {
        String input = String.format("position[minX=%1$d,maxX=%2$d,minY=%3$d,maxY=%4$d,minZ=%5$d,maxZ=%6$d]", -15, 25, 64, 123, -12, 36);
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        PositionLimit limit = (PositionLimit) parser.createInstance();
        assertEquals(new PositionLimit(-15, 25, 64, 123, -12, 36), limit);
    }

    @Test
    void createPositionType2() throws CommandSyntaxException {
        String input = String.format("position[minX=%1$d,maxY=%4$d,maxX=%2$d,minZ=%5$d,maxZ=%6$d,minY=%3$d]", -15, 25, 64, 123, -12, 36);
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        PositionLimit limit = (PositionLimit) parser.createInstance();
        assertEquals(new PositionLimit(-15, 25, 64, 123, -12, 36), limit);
    }

    @Test
    void createPositionType3() throws CommandSyntaxException {
        String input = String.format("position[minX=%1$d, maxY=%4$d, maxX=%2$d, minZ=%5$d, maxZ=%6$d, minY=%3$d]", -15, 25, 64, 1232654, -12, 36);
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        PositionLimit limit = (PositionLimit) parser.createInstance();
        assertEquals(new PositionLimit(-15, 25, 64, 1232654, -12, 36), limit);
    }

    @ParameterizedTest
    @ValueSource(strings = {"overworld", "minecraft:overworld", "the_nether", "minecraft:the_end"})
    void createDimensionType(String dim) throws CommandSyntaxException {
        String input = String.format("dimension[dim=%s]", dim);
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertDoesNotThrow(parser::parse);
        assertDoesNotThrow(parser::createInstance);
        DimensionLimit limit = (DimensionLimit) parser.createInstance();
        assertEquals(DimensionLimit.fromName(dim), limit);
    }
}
