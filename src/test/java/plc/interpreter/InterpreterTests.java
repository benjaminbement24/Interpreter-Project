package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

final class InterpreterTests {

    @Test
    void testTerm() {
        test(new Ast.Term("print", Arrays.asList()), Interpreter.VOID, Collections.emptyMap());
    }

    @Test
    void testIdentifier() {
        test(new Ast.Identifier("num"), 10, Collections.singletonMap("num", 10));
    }

    @Test
    void testNumber() {
        test(new Ast.NumberLiteral(BigDecimal.ONE), BigDecimal.ONE, Collections.emptyMap());
    }

    @Test
    void testString() {
        test(new Ast.StringLiteral("string"), "string", Collections.emptyMap());
    }

    @ParameterizedTest
    @MethodSource
    void testAddition(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testAddition() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("+", Arrays.asList()), BigDecimal.valueOf(0)),
                Arguments.of("Single Argument", new Ast.Term("+", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )), BigDecimal.valueOf(10)),
                Arguments.of("Multiple Arguments", new Ast.Term("+", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(6)),
                Arguments.of("Term Arguments", new Ast.Term("+", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN),
                        new Ast.Term("+", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(-3)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-15))
                        ))
                )), BigDecimal.valueOf(-8))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testSubtraction(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testSubtraction() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("-", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE)
                )), BigDecimal.valueOf(-1)),
                Arguments.of("Multiple Arguments", new Ast.Term("-", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ONE),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(-4)),
                Arguments.of("Term Arguments", new Ast.Term("-", Arrays.asList(
                        new Ast.Term("-", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.ONE),
                                new Ast.NumberLiteral(BigDecimal.valueOf(2))
                        ))
                )), BigDecimal.ONE)
        );
    }

    @ParameterizedTest
    @MethodSource
    void testMultiplication(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testMultiplication() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("*", Arrays.asList()), BigDecimal.valueOf(1)),
                Arguments.of("Single Argument", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(-9.000))
                )), BigDecimal.valueOf(-9.000)),
                Arguments.of("Multiple Arguments", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(6)),
                Arguments.of("Term Arguments", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(4)),
                        new Ast.Term("*", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.TEN),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-2))
                        ))
                )), BigDecimal.valueOf(-400)),
                Arguments.of("Decimal and Negative Arguments", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(-2.5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), BigDecimal.valueOf(-5.0)),
                Arguments.of("Multiple Decimal Arguments", new Ast.Term("*", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0.15)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(0.25))
                )), BigDecimal.valueOf(0.0375))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testDivision(String test, Ast ast, BigDecimal expected) {
        test(ast, expected, Collections.emptyMap());
    }

    private static Stream<Arguments> testDivision() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term("/", Arrays.asList()), null),
                Arguments.of("Single Argument", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(-2.000))
                )), BigDecimal.valueOf(0)),
                Arguments.of("Multiple Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1.000).setScale(3)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), BigDecimal.valueOf(0.167)),
                Arguments.of("Term Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(400)),
                        new Ast.Term("/", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(30)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-3))
                        ))
                )), BigDecimal.valueOf(-200)),
                Arguments.of("Zero Arg", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.ZERO)
                )), null),
                Arguments.of("Decimal and Negative Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(-5.00)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(2))
                )), BigDecimal.valueOf(-2.50)),
                Arguments.of("Multiple Decimal Arguments", new Ast.Term("/", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0.65)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(0.34))
                )), BigDecimal.valueOf(1.91))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testLessThan(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>() {{
            put("x", BigDecimal.valueOf(5));
        }});
    }

    private static Stream<Arguments> testLessThan() {
        return Stream.of(
                Arguments.of("< true", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.Identifier("true")
                )), new Boolean(true)),
                Arguments.of("Zero Arguments", new Ast.Term("<", Arrays.asList()), new Boolean(true)),
                Arguments.of("0 < x < 10", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10))
                )), new Boolean(true)),
                Arguments.of("0 < x < -1", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                )), new Boolean(false)),
                Arguments.of("0 < x < 10", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10))
                )), new Boolean(true))

        );
    }


    @ParameterizedTest
    @MethodSource
    void testLessThanEqual(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>() {{
            put("x", BigDecimal.valueOf(5));
        }});
    }

    private static Stream<Arguments> testLessThanEqual() {
        return Stream.of(
                Arguments.of("<= true", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.Identifier("true")
                )), new Boolean(true)),
                Arguments.of("1 <= 0 <= INVALID", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("INVALID")
                )), new Boolean(false)),
                Arguments.of("<= ARGS", new Ast.Term("<=", Arrays.asList(
                        new Ast.Identifier("ARGS")
                )), new Boolean(true)),
                Arguments.of("0 <= x <= 10", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10))
                )), new Boolean(true)),
                Arguments.of("Zero Arguments", new Ast.Term("<=", Arrays.asList()), new Boolean(true)),
                Arguments.of("0 <= x <= -1", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                )), new Boolean(false)),
                Arguments.of("6 <= x <= 6 ", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(6)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(6))
                )), new Boolean(false)),
                Arguments.of("<= 1 5 5 7", new Ast.Term("<=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(7))
                )), new Boolean(true))

        );
    }

    @ParameterizedTest
    @MethodSource
    void testGreaterThan(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>() {{
            put("x", BigDecimal.valueOf(5));
        }});
    }

    private static Stream<Arguments> testGreaterThan() {
        return Stream.of(
                Arguments.of("> true", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(1)),
                        new Ast.Identifier("true")
                )), new Boolean(true)),
                Arguments.of("> 6 5 4 3", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(6)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(4)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(3))
                )), new Boolean(true)),
                Arguments.of("Zero Arguments", new Ast.Term(">", Arrays.asList()), new Boolean(true)),
                Arguments.of("11 > 5 > 10", new Ast.Term("<", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(11)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(10))
                )), new Boolean(false)),
                Arguments.of("6 > x > -1", new Ast.Term(">", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(6)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                )), new Boolean(true))

        );
    }

    @ParameterizedTest
    @MethodSource
    void testGreaterThanEqual(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>() {{
            put("x", BigDecimal.valueOf(6));
        }});
    }

    private static Stream<Arguments> testGreaterThanEqual() {
        return Stream.of(
                Arguments.of("Zero Arguments", new Ast.Term(">=", Arrays.asList()), new Boolean(true)),
                Arguments.of("10 >= x >= 5", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(10)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5))
                )), new Boolean(true)),
                Arguments.of("0 >= x >= -1", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                )), new Boolean(false)),
                Arguments.of("6 >= 6 >= 6 ", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(6)),
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.valueOf(6))
                )), new Boolean(true)),
                Arguments.of(">= 7 5 5 1", new Ast.Term(">=", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.valueOf(7)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                        new Ast.NumberLiteral(BigDecimal.valueOf(1))
                )), new Boolean(true))

        );
    }


































    @ParameterizedTest
    @MethodSource
    void testEquals(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>() {{
            put("x", BigDecimal.valueOf(5));
            put("y", BigDecimal.valueOf(5));
        }});
    }

    private static Stream<Arguments> testEquals() {
        return Stream.of(
                //Arguments.of("Zero Arguments", new Ast.Term("equals?", Arrays.asList()), new Boolean(true)),
                Arguments.of("equals? x y", new Ast.Term("equals?", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.Identifier("y")
                )), new Boolean(true))

        );
    }

    @ParameterizedTest
    @MethodSource
    void testTrueFalse(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>());
    }

    private static Stream<Arguments> testTrueFalse() {
        return Stream.of(
                Arguments.of("Only True", new Ast.Identifier("true"), new Boolean(true)),
                Arguments.of("Only False", new Ast.Identifier("false"), new Boolean(false))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testNot(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>());
    }

    private static Stream<Arguments> testNot() {
        return Stream.of(
                Arguments.of("Opposite of True", new Ast.Term("not", Arrays.asList(
                        new Ast.Identifier("true")
                )), new Boolean(false)),

                Arguments.of("Advanced Test", new Ast.Term("not", Arrays.asList(
                        new Ast.Term("<", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(10))
                        ))
                )), new Boolean(false))
        );
    }


    @ParameterizedTest
    @MethodSource
    void testAnd(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>());
    }

    private static Stream<Arguments> testAnd() {
        return Stream.of(
                Arguments.of("And True", new Ast.Term("and", Arrays.asList(
                        new Ast.Identifier("true")
                )), new Boolean(true)),

                Arguments.of("Advanced Test", new Ast.Term("and", Arrays.asList(
                        new Ast.Term("<", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(10))
                        )),
                        new Ast.Term(">", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                        ))

                )), new Boolean(false))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testOr(String test, Ast ast, Boolean expected) {
        test(ast, expected, new HashMap<String,Object>());
    }

    private static Stream<Arguments> testOr() {
        return Stream.of(
                Arguments.of("Or True", new Ast.Term("or", Arrays.asList(
                        new Ast.Identifier("true")
                )), new Boolean(true)),

                Arguments.of("Advanced Test", new Ast.Term("or", Arrays.asList(
                        new Ast.Term("<", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(10))
                        )),
                        new Ast.Term(">", Arrays.asList(
                                new Ast.NumberLiteral(BigDecimal.valueOf(0)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(5)),
                                new Ast.NumberLiteral(BigDecimal.valueOf(-1))
                        ))

                )), new Boolean(false))
        );
    }



    private static void test(Ast ast, Object expected, Map<String, Object> map) {
        Scope scope = new Scope(null);
        map.forEach(scope::define);
        Interpreter interpreter = new Interpreter(new PrintWriter(System.out), scope);
        if (expected != null) {
            Assertions.assertEquals(expected, interpreter.eval(ast));
        } else {
            Assertions.assertThrows(EvalException.class, () -> interpreter.eval(ast));
        }
    }

}
