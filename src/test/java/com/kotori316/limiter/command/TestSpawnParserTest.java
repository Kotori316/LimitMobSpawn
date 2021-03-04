package com.kotori316.limiter.command;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import javax.annotation.Nonnull;
import net.minecraft.entity.SpawnReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.kotori316.limiter.BeforeAllTest;
import com.kotori316.limiter.TestSpawn;
import com.kotori316.limiter.conditions.All;
import com.kotori316.limiter.conditions.And;
import com.kotori316.limiter.conditions.DimensionLimit;
import com.kotori316.limiter.conditions.Not;
import com.kotori316.limiter.conditions.Or;
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
        assertAll(Stream.of("all", "classification", "position", "entity").map(
            s -> () -> assertTrue(suggestions.getList().stream().anyMatch(su -> su.getText().equals(s)), "Suggestion of " + s + " whole:" + suggestions)
        ));
        assertFalse(suggestions.getList().stream().map(Suggestion::getText).anyMatch(Predicate.isEqual("anonymous")));
    }

    static class AllParse {
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
        void createAllType() throws CommandSyntaxException {
            String input = "all[]";
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn testSpawn = parser.createInstance();
            assertEquals(All.getInstance(), testSpawn);
        }
    }

    static class SpawnParse {
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

        @ParameterizedTest
        @ValueSource(strings = {"", "event1", "force"})
        void invalidValues(String name) {
            String input = String.format("spawn_reason[spawn_reason=%s]", name);
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertThrows(CommandSyntaxException.class, parser::parse);
        }

        @Test
        void suggestSpawnType1() throws ExecutionException, InterruptedException {
            String input = "spawn_reason[";
            assertEquals(Collections.singleton("spawn_reason"), getSuggestions(input));
        }
    }

    static class PositionParse {
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

        @Test
        void suggestPositionKeys1() throws ExecutionException, InterruptedException {
            String input = "position[";
            assertEquals(Sets.newHashSet("minX", "maxX", "minY", "maxY", "minZ", "maxZ"), getSuggestions(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"position[minX=1,", "position[minX=1, "})
        void suggestPositionRestKeys(String input) throws ExecutionException, InterruptedException {
            assertEquals(Sets.newHashSet("maxX", "minY", "maxY", "minZ", "maxZ"), getSuggestions(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"position[minX=", "position[maxX="})
        void suggestPositionNothing(String input) throws ExecutionException, InterruptedException {
            assertEquals(Collections.emptySet(), getSuggestions(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"position[minX=1", "position[maxX=15", "position[minZ=-15", "position[minX=1,minY=3"})
        void suggestPositionComma(String input) throws ExecutionException, InterruptedException {
            assertEquals(Collections.singleton(","), getSuggestions(input));
        }

        @ParameterizedTest
        @ValueSource(strings = {"position[minX=1,minY=3,", "position[minX=1,minY=3, ", "position[minX=1, minY=4,", "position[minX=1, minY=4, "})
        void suggestPositionRestKey2(String input) throws ExecutionException, InterruptedException {
            assertEquals(Sets.newHashSet("maxX", "maxY", "minZ", "maxZ"), getSuggestions(input));
        }
    }

    @Nonnull
    private static Set<String> getSuggestions(String input) throws InterruptedException, ExecutionException {
        TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
        assertThrows(CommandSyntaxException.class, parser::parse);
        return parser.getSuggestion(new SuggestionsBuilder(input, 0)).get().getList().stream().map(Suggestion::getText).collect(Collectors.toSet());
    }

    static class DimensionParse {
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

        @Test
        void getDimensionSuggestion() throws ExecutionException, InterruptedException {
            String input = "dimension[";
            assertEquals(Collections.singleton("dim"), getSuggestions(input));
        }
    }

    static class AndParse {
        @ParameterizedTest
        @ValueSource(strings = {
            "and[dimension[dim=overworld] spawn_reason[spawn_reason=natural]]",
            "and[dimension[dim=overworld], spawn_reason[spawn_reason=natural]]",
            "and[dimension[dim=overworld]spawn_reason[spawn_reason=natural]]",
            "and[dimension[dim=overworld]spawn_reason[spawn_reason=natural], ]",
            "and[dimension[dim=overworld], spawn_reason[spawn_reason=natural],]",
        })
        void parse1(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            And and = (And) parser.createInstance();
            And expected = new And(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL));
            assertEquals(expected, and);
        }

        static String[] parse2Arg() {
            String pos = String.format("position[minX=%1$d,maxX=%2$d,minY=%3$d,maxY=%4$d,minZ=%5$d,maxZ=%6$d]", -15, 25, 64, 123, -12, 36);
            return new String[]{
                "and[dimension[dim=overworld] spawn_reason[spawn_reason=natural] " + pos + "]",
                "and[dimension[dim=overworld], spawn_reason[spawn_reason=natural], " + pos + "]",
                "and[dimension[dim=overworld]spawn_reason[spawn_reason=natural]" + pos + "]",
            };
        }

        @ParameterizedTest
        @MethodSource("parse2Arg")
        void parse2(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            And and = (And) parser.createInstance();
            And expected = new And(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL),
                new PositionLimit(-15, 25, 64, 123, -12, 36));
            assertEquals(expected, and);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "and[dimension[dim=overworld] not[spawn_reason[spawn_reason=natural]]]",
            "and[dimension[dim=overworld], not[spawn_reason[spawn_reason=natural]]]",
            "and[dimension[dim=overworld]not[spawn_reason[spawn_reason=natural]]]",
            "and[dimension[dim=overworld]not[spawn_reason[spawn_reason=natural]], ]",
            "and[dimension[dim=overworld], not[spawn_reason[spawn_reason=natural]],]",
        })
        void parse3(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            And and = (And) parser.createInstance();
            And expected = new And(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL).not());
            assertEquals(expected, and);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "and[not[dimension[dim=overworld]] spawn_reason[spawn_reason=natural]]",
            "and[not[dimension[dim=overworld]], spawn_reason[spawn_reason=natural]]",
            "and[not[dimension[dim=overworld]]spawn_reason[spawn_reason=natural]]",
            "and[not[dimension[dim=overworld]]spawn_reason[spawn_reason=natural], ]",
            "and[not[dimension[dim=overworld]], spawn_reason[spawn_reason=natural],]",
        })
        void parse4(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            And and = (And) parser.createInstance();
            And expected = new And(DimensionLimit.fromName("overworld").not(), new SpawnReasonLimit(SpawnReason.NATURAL));
            assertEquals(expected, and);
        }
    }

    static class OrParse {
        @ParameterizedTest
        @ValueSource(strings = {
            "or[dimension[dim=overworld] spawn_reason[spawn_reason=natural]]",
            "or[dimension[dim=overworld], spawn_reason[spawn_reason=natural]]",
            "or[dimension[dim=overworld]spawn_reason[spawn_reason=natural]]",
            "or[dimension[dim=overworld]spawn_reason[spawn_reason=natural], ]",
            "or[dimension[dim=overworld], spawn_reason[spawn_reason=natural],]",
        })
        void parse1(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn and = parser.createInstance();
            Or expected = new Or(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL));
            assertEquals(expected, and);
        }

        static String[] parse2Arg() {
            String pos = String.format("position[minX=%1$d,maxX=%2$d,minY=%3$d,maxY=%4$d,minZ=%5$d,maxZ=%6$d]", -15, 25, 64, 123, -12, 36);
            return new String[]{
                "or[dimension[dim=overworld] spawn_reason[spawn_reason=natural] " + pos + "]",
                "or[dimension[dim=overworld], spawn_reason[spawn_reason=natural], " + pos + "]",
                "or[dimension[dim=overworld]spawn_reason[spawn_reason=natural]" + pos + "]",
                "or[dimension[dim=overworld]spawn_reason[spawn_reason=natural]" + pos + ", ]",
            };
        }

        @ParameterizedTest
        @MethodSource("parse2Arg")
        void parse2(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn and = parser.createInstance();
            Or expected = new Or(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL),
                new PositionLimit(-15, 25, 64, 123, -12, 36));
            assertEquals(expected, and);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "or[dimension[dim=overworld] not[spawn_reason[spawn_reason=natural]]]",
            "or[dimension[dim=overworld], not[spawn_reason[spawn_reason=natural]]]",
            "or[dimension[dim=overworld]not[spawn_reason[spawn_reason=natural]]]",
            "or[dimension[dim=overworld]not[spawn_reason[spawn_reason=natural]], ]",
            "or[dimension[dim=overworld], not[spawn_reason[spawn_reason=natural]],]",
        })
        void parse3(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn and = parser.createInstance();
            Or expected = new Or(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL).not());
            assertEquals(expected, and);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "or[not[dimension[dim=overworld]] spawn_reason[spawn_reason=natural]]",
            "or[not[dimension[dim=overworld]], spawn_reason[spawn_reason=natural]]",
            "or[not[dimension[dim=overworld]]spawn_reason[spawn_reason=natural]]",
            "or[not[dimension[dim=overworld]]spawn_reason[spawn_reason=natural], ]",
            "or[not[dimension[dim=overworld]], spawn_reason[spawn_reason=natural],]",
        })
        void parse4(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn and = parser.createInstance();
            Or expected = new Or(DimensionLimit.fromName("overworld").not(), new SpawnReasonLimit(SpawnReason.NATURAL));
            assertEquals(expected, and);
        }
    }

    static class NotParse {
        @ParameterizedTest
        @ValueSource(strings = {
            "not[dimension[dim=overworld]]",
            "not[ dimension[dim=overworld]]",
            "not[ dimension[dim=overworld] ]",
            "not[dimension[dim=overworld] ]",
            "not[  dimension[dim=overworld]  ]",
        })
        void parse1(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            Not expected = new Not(DimensionLimit.fromName("overworld"));
            assertEquals(expected, parser.createInstance());
        }

        @Test
        void parse2() throws CommandSyntaxException {
            String input = String.format("not[position[minX=%1$d,maxX=%2$d,minY=%3$d,maxY=%4$d,minZ=%5$d,maxZ=%6$d]]", -15, 25, 64, 123, -12, 36);
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn expected = new PositionLimit(-15, 25, 64, 123, -12, 36).not();
            assertEquals(expected, parser.createInstance());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "not[and[dimension[dim=overworld], spawn_reason[spawn_reason=natural]]]",
            "not[ and[dimension[dim=overworld] spawn_reason[spawn_reason=natural]  ]   ]",
            "not[ and[dimension[dim=overworld], spawn_reason[spawn_reason=natural],  ]   ]",
            "not[ and[dimension[dim=overworld],   spawn_reason[spawn_reason=natural],  ]   ]",
        })
        void parse3(String input) throws CommandSyntaxException {
            TestSpawnParser parser = new TestSpawnParser(new StringReader(input));
            assertDoesNotThrow(parser::parse);
            assertDoesNotThrow(parser::createInstance);
            TestSpawn expected = new And(DimensionLimit.fromName("overworld"), new SpawnReasonLimit(SpawnReason.NATURAL)).not();
            assertEquals(expected, parser.createInstance());
        }
    }
}
