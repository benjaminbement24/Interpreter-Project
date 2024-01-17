package plc.compiler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Standard JUnit5 parameterized tests. See the RegexTests file from 
 * the interpreter part 1 for more information.
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
                Arguments.of("is-empty?", false),
                Arguments.of("<=>", false),
                Arguments.of("42=life", false),
                Arguments.of("why,are,there,commas,", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testInteger(String input, boolean success) {
        test(input, Token.Type.INTEGER, success);
    }

    private static Stream<Arguments> testInteger() {
        return Stream.of(
                Arguments.of("1", true),
                Arguments.of("-1.0", false),
                Arguments.of("007.000", false),
                Arguments.of("1.", false),
                Arguments.of(".5", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDecimal(String input, boolean success) {
        test(input, Token.Type.DECIMAL, success);
    }

    private static Stream<Arguments> testDecimal() {
        return Stream.of(
                Arguments.of("1", false),
                Arguments.of("-1.0", false),
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
                Arguments.of("\"unterminated", false),
                Arguments.of("\"invalid\\escape\"", true),
                Arguments.of("\"escape\\\"", true),
                Arguments.of("\"escaped\\\"quote\"", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOperator(String input, boolean success) {
        test(input, Token.Type.OPERATOR, success);
    }

    private static Stream<Arguments> testOperator() {
        return Stream.of(
                Arguments.of("==", true),
                Arguments.of("!=", true),
                Arguments.of("\f", true)
        );
    }

    /*@Test
    void testExample1() {
        String input = "#lang racket\n\n(define {fizzbuzz n)\n (for ([i (in-range 1 (+ n 1))])\n (displayln\n (match (gcd i 15)\n [15 \"FizzBuzz\"]\n [3 \"Fizz\"]\n [5 \"Buzz\"]\n [_ i])))\n \n(fizzbuzz 100)";
        List<plc.compiler.Token> expected = Arrays.asList(
                new plc.interpreter.Token(plc.interpreter.Token.Type.OPERATOR, "#lang", 0),
                new plc.interpreter.Token(plc.interpreter.Token.Type.IDENTIFIER, "racket", 1),
                new plc.interpreter.Token(plc.interpreter.Token.Type.NUMBER, "1", 3),
                new plc.interpreter.Token(plc.interpreter.Token.Type.NUMBER, "-2.0", 5),
                new plc.interpreter.Token(plc.interpreter.Token.Type.OPERATOR, ")", 9)
        );
        Assertions.assertEquals(expected, plc.interpreter.Lexer.lex(input));
    }*/

    @Test
    void testExample2(){
        String input = "1.";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.INTEGER, "1",0),
                new Token(Token.Type.OPERATOR, ".",1)
        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }

    @Test
    void DoubleDecimal(){
        String input = "1..0";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.INTEGER, "1",0),
                new Token(Token.Type.OPERATOR, ".",1),
                new Token(Token.Type.OPERATOR, ".",2),
                new Token(Token.Type.INTEGER, "0",3)

        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }

    @Test
    void EqualsCombination(){
        String input = "!====";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.OPERATOR, "!=",0),
                new Token(Token.Type.OPERATOR, "==",2),
                new Token(Token.Type.OPERATOR, "=",4)

        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }

    @Test
    void Assignment(){
        String input = "iden = expr;";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "iden",0),
                new Token(Token.Type.OPERATOR, "=",5),
                new Token(Token.Type.IDENTIFIER, "expr",7),
                new Token(Token.Type.OPERATOR, ";", 11)

        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }

    @Test
    void Program1(){
        String input = "LET first: INTEGER = 1;\nWHILE first != 10 DO\nPRINT(first);\nfirst = first + 1;\nEND";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "Too Lazy to fill in test",0)
        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }

    @Test
    void Program2(){
        String input = "LET i;\nLET first: INTEGER = 1;\nPRINT(\"n=\");\nWHILE i != n DO\nLET zero: INTEGER = n - i * (n / i);\nIF zero == 0 THEN\nPRINT(i, \" * \");\nn = n / i;\n ELSE\ni = i + 1;\nEND\nEND\nPRINT(i);";
        List<plc.compiler.Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "Too Lazy to fill in test",0)
        );
        Assertions.assertEquals(expected, plc.compiler.Lexer.lex(input));
    }




    @Test
    void testmultipleweirdStrings() {
        String input = "afdsf223 !=";
        List<Token> expected = Arrays.asList(
                new Token(Token.Type.IDENTIFIER, "afdsf223", 0),
                new Token(Token.Type.OPERATOR, "!=", 9)
        );
        Assertions.assertEquals(expected, Lexer.lex(input));
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
