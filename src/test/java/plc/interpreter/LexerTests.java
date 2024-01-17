package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from part 1 for
 * more information.
 */
final class LexerTests {

    @ParameterizedTest
    @MethodSource
    void testIdentifier(String input, boolean success) {
        test(input, Token.Type.IDENTIFIER, success);
    }

    private static Stream<Arguments> testIdentifier() {
        return Stream.of(
                Arguments.of("getName", true),
                Arguments.of("is-empty?", true),
                Arguments.of("<=>", true),
                Arguments.of("..", true),
                Arguments.of("$", false),
                Arguments.of("why,are,there,commas,", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNumber(String input, boolean success) {
        test(input, Token.Type.NUMBER, success);
    }

    private static Stream<Arguments> testNumber() {
        return Stream.of(
                Arguments.of("1", true),
                Arguments.of("-1.0", true),
                Arguments.of("007.000", true),
                Arguments.of("1.", false),
                Arguments.of(".5", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testString(String input, boolean success) {
        test(input, Token.Type.STRING, success);
    }

    private static Stream<Arguments> testString() {
        return Stream.of(
                Arguments.of("\"\"", true),
                Arguments.of("\"abc\"", true),
                Arguments.of("\"Hello,\\nWorld\"", true),
                Arguments.of("\"Hello,World\"", true),
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid\\escape\"", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String input, boolean success) {
        test(input, Token.Type.OPERATOR, success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("(", true),
                Arguments.of("#", true),
                //Arguments.of("@.#", true),
                Arguments.of(".", true),
                Arguments.of(" ", false)
                //Arguments.of("\t", true)
        );
    }

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "+", 1),
                new Token(Token.Type.NUMBER, "1", 3),
                new Token(Token.Type.NUMBER, "-2.0", 5),
                new Token(Token.Type.OPERATOR, ")", 9)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello\\\"World!\")";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "print", 1),
                new Token(Token.Type.STRING, "\"Hello\\\"World!\"", 7),
                new Token(Token.Type.OPERATOR, ")", 22)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testMarc() {
        String input = "\"\\b\\n\\r\\t\\\'\\\"\\\\\"";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.STRING, "\"\\b\\n\\r\\t\\\'\\\"\\\\\"", 0)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "(", 0),
                new Token(Token.Type.IDENTIFIER, "let", 1),
                new Token(Token.Type.OPERATOR, "[", 5),
                new Token(Token.Type.IDENTIFIER, "x", 6),
                new Token(Token.Type.NUMBER, "10", 8),
                new Token(Token.Type.OPERATOR, "]", 10),
                new Token(Token.Type.OPERATOR, "(", 12),
                new Token(Token.Type.IDENTIFIER, "assert-equals?", 13),
                new Token(Token.Type.IDENTIFIER, "x", 28),
                new Token(Token.Type.NUMBER, "10", 30),
                new Token(Token.Type.OPERATOR, ")", 32),
                new Token(Token.Type.OPERATOR, ")", 33)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void testExampleAfter() {
        String input = "\"\"";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "@", 0),
                new Token(Token.Type.OPERATOR, ".", 1),
                new Token(Token.Type.OPERATOR, "#", 2)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }
    @Test
    void testExampleme() {
        String input = "..";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "..", 0)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }


    @Test
    void testExampleWeirdAssString() {
        String input = "one\ntwo";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "one", 0),
                new Token(Token.Type.IDENTIFIER, "two", 4)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }

    @Test
    void symbols() {
        String input = "symbols+-*/.:!?<>=";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "symbols+-*/.:!?<>=", 0)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }


    @Test
    void test18() {
        String input = "#lang racket\n\n(define (fizzbuzz n) \n (for ([i (in-range 1 (+ n 1))])\n    (displayln\n      (match (gcd i 15)\n        [15 \"FizzBuzz\"]\n        [3 \"Fizz\"]\n        [5 \"Buzz\"]\n        [_ i])))\n\n(fizzbuzz 100)";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "#", 0),
                new Token(Token.Type.IDENTIFIER, "lang", 1),
                new Token(Token.Type.IDENTIFIER, "racket", 6),
                new Token(Token.Type.OPERATOR, "(", 14),
                new Token(Token.Type.IDENTIFIER, "define", 15),
                new Token(Token.Type.OPERATOR, "(", 22),
                new Token(Token.Type.IDENTIFIER, "fizzbuzz", 23),
                new Token(Token.Type.IDENTIFIER, "n", 32),
                new Token(Token.Type.OPERATOR, ")", 33),
                new Token(Token.Type.OPERATOR, "(", 37),
                new Token(Token.Type.IDENTIFIER, "for", 38),
                new Token(Token.Type.OPERATOR, "(", 42),
                new Token(Token.Type.OPERATOR, "[", 43),
                new Token(Token.Type.IDENTIFIER, "i", 44),
                new Token(Token.Type.OPERATOR, "(", 46),
                new Token(Token.Type.IDENTIFIER, "in-range", 47),
                new Token(Token.Type.NUMBER, "1", 56),
                new Token(Token.Type.OPERATOR, "(",  58),
                new Token(Token.Type.IDENTIFIER, "+", 59),
                new Token(Token.Type.IDENTIFIER, "n", 61),
                new Token(Token.Type.NUMBER, "1", 63),
                new Token(Token.Type.OPERATOR, ")", 64),
                new Token(Token.Type.OPERATOR, ")", 65),
                new Token(Token.Type.OPERATOR, "]", 66),
                new Token(Token.Type.OPERATOR, ")", 67),
                new Token(Token.Type.OPERATOR, "(", 73),
                new Token(Token.Type.IDENTIFIER, "displayln", 74),
                new Token(Token.Type.OPERATOR, "(", 90),
                new Token(Token.Type.IDENTIFIER, "match", 91),
                new Token(Token.Type.OPERATOR, "(", 97),
                new Token(Token.Type.IDENTIFIER, "gcd", 98),
                new Token(Token.Type.IDENTIFIER, "i", 102),
                new Token(Token.Type.NUMBER, "15", 104),
                new Token(Token.Type.OPERATOR, ")", 106),
                new Token(Token.Type.OPERATOR, "[", 116),
                new Token(Token.Type.NUMBER, "15", 117),
                new Token(Token.Type.STRING, "\"FizzBuzz\"", 120),
                new Token(Token.Type.OPERATOR, "]", 130),
                new Token(Token.Type.OPERATOR, "[", 140),
                new Token(Token.Type.NUMBER, "3", 141),
                new Token(Token.Type.STRING, "\"Fizz\"", 143),
                new Token(Token.Type.OPERATOR, "]", 149),
                new Token(Token.Type.OPERATOR, "[",159),
                new Token(Token.Type.NUMBER, "5", 160),
                new Token(Token.Type.STRING, "\"Buzz\"", 162),
                new Token(Token.Type.OPERATOR, "]", 168),
                new Token(Token.Type.OPERATOR, "[", 178),
                new Token(Token.Type.IDENTIFIER, "_", 179),
                new Token(Token.Type.IDENTIFIER, "i", 181),
                new Token(Token.Type.OPERATOR, "]", 182),
                new Token(Token.Type.OPERATOR, ")", 183),
                new Token(Token.Type.OPERATOR, ")", 184),
                new Token(Token.Type.OPERATOR, ")", 185),
                new Token(Token.Type.OPERATOR, "(", 188),
                new Token(Token.Type.IDENTIFIER, "fizzbuzz", 189),
                new Token(Token.Type.NUMBER, "100", 198),
                new Token(Token.Type.OPERATOR, ")", 201)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
    }







    @ParameterizedTest
    @MethodSource("plc.interpreter.LexerTests#testPeekAndMatch")
    void testPeek(String test, String input, String[] patterns, boolean matches) {
        Lexer lexer = new Lexer(input);
        Assertions.assertEquals(matches, lexer.peek(patterns));
        Assertions.assertEquals(0, lexer.chars.index);
    }

    @ParameterizedTest
    @MethodSource("plc.interpreter.LexerTests#testPeekAndMatch")
    void testMatch(String test, String input, String[] patterns, boolean matches) {
        Lexer lexer = new Lexer(input);
        Assertions.assertEquals(matches, lexer.match(patterns));
        Assertions.assertEquals(matches ? patterns.length : 0, lexer.chars.index);
    }

    private static Stream<Arguments> testPeekAndMatch() {
        return Stream.of(
                Arguments.of("Single Char Input, Single Char Pattern", "a", new String[] {"a"}, true),
                Arguments.of("Multiple Char Input, Single Char Pattern", "abc", new String[] {"a"}, true),
                Arguments.of("Single Char Input, Multiple Char Pattern", "a", new String[] {"a", "b", "c"}, false),
                Arguments.of("Multiple Char Input, Multiple Char Pattern", "abc", new String[] {"a"}, true),
                Arguments.of("Single Char Input, Char Class Pattern Success", "a", new String[] {"[a-z]"}, true),
                Arguments.of("Single Char Input, Char Class Pattern Failure", "@", new String[] {"[a-z]"}, false),
                Arguments.of("Multiple Char Input, Mixed Pattern Success", "cat", new String[] {"c", "[aeiou]", "t"}, true),
                Arguments.of("Multiple Char Input, Mixed Pattern Failure 1", "cyt", new String[] {"c", "[aeiou]", "t"}, false),
                Arguments.of("Multiple Char Input, Mixed Pattern Failure 2", "cow", new String[] {"c", "[aeiou]", "t"}, false),
                Arguments.of("End of Input", "eo", new String[] {"e", "o", "[fi]"}, false)
        );
    }

    @Test
    void testCharStream() {
        Lexer lexer = new Lexer("abc 123");
        lexer.chars.advance();
        lexer.chars.advance();
        lexer.chars.advance();
        Assertions.assertEquals(new Token(Token.Type.IDENTIFIER, "abc", 0), lexer.chars.emit(Token.Type.IDENTIFIER));
        lexer.chars.advance();
        lexer.chars.reset();
        Assertions.assertEquals(0, lexer.chars.length);
        lexer.chars.advance();
        lexer.chars.advance();
        lexer.chars.advance();
        Assertions.assertEquals(new Token(Token.Type.NUMBER, "123", 4), lexer.chars.emit(Token.Type.NUMBER));
        Assertions.assertFalse(lexer.chars.has(0));
    }

    /**
     * Tests that the input lexes to the (single) expected token if successful,
     * else throws a {@link ParseException} otherwise.
     */
    private static void test(String input, Token.Type expected, boolean success) {
        try {
            if (success) {
                Assertions.assertEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
            } else {
                Assertions.assertNotEquals(Arrays.asList(new Token(expected, input, 0)), Lexer.lex(input));
            }
        } catch (ParseException e) {
            Assertions.assertFalse(success, e.getMessage());
        }
    }

}
