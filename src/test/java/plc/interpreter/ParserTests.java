package plc.interpreter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * You know the drill...
 */
final class ParserTests {

    @Test
    void testExample1() {
        String input = "(+ 1 -2.0)";
        Ast expected = new Ast.Term("+", Arrays.asList(
                new Ast.NumberLiteral(BigDecimal.ONE),
                new Ast.NumberLiteral(BigDecimal.valueOf(-2.0))
        ));
        test(input, expected);
    }

    @Test
    void testExample2() {
        String input = "(print \"Hello,\\\"World!\")";
        Ast expected = new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("Hello,\"World!")
        ));
        test(input, expected);
    }

    @Test
    void testExample3() {
        String input = "(let [x 10] (assert-equals? x 10))";
        Ast expected = new Ast.Term("let", Arrays.asList(
                new Ast.Term("x", Arrays.asList(
                        new Ast.NumberLiteral(BigDecimal.TEN)
                )),
                new Ast.Term("assert-equals?", Arrays.asList(
                        new Ast.Identifier("x"),
                        new Ast.NumberLiteral(BigDecimal.TEN)
                ))
        ));
        test(input, expected);
    }

    @Test
    void testExample4() {
        String input = "(print x) (print y) (print z)";
        List<Ast> expected = Arrays.asList(
                new Ast.Term("print",Arrays.asList(new Ast.Identifier("x"))),
                new Ast.Term("print",Arrays.asList(new Ast.Identifier("y"))),
                new Ast.Term("print",Arrays.asList(new Ast.Identifier("z")))
        );
        test2(input, expected);
    }

    @Test
    void testExample5() {
        String input = "x [x 10]";
        List<Ast> expected = Arrays.asList( new Ast.Identifier("x"), new Ast.Term("x", Arrays.asList(new Ast.NumberLiteral(BigDecimal.TEN))));
        test2(input, expected);
    }

    @Test
    void testExample6() {
        String input = "(print \"\\b\\n\\r\\t\\\'\\\"\\\\\")";
        Ast expected = new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("\"\\b\\n\\r\\t\\\'\\\"\\\\\"")
        ));
        test(input, expected);
    }

    @Test
    void testExample7() {
        List<Ast> empty = new ArrayList<>();
        String input = "(print (x 10)";
        Ast emptyast = new Ast.Term("print", Arrays.asList(
                new Ast.Term("x",Arrays.asList(new Ast.NumberLiteral(BigDecimal.TEN)))
        ));
        test(input, emptyast);
    }

    @Test
    void testExample8() {
        List<Ast> empty = new ArrayList<>();
        String input = "(print (10 10))";
        Ast emptyast = new Ast.Term("print", Arrays.asList(
                new Ast.Term("10",Arrays.asList(new Ast.NumberLiteral(BigDecimal.TEN)))
        ));
        test(input, emptyast);
    }

    @Test
    void testExample9() {
        List<Ast> empty = new ArrayList<>();
        String input = "(print ())";
        Ast emptyast = new Ast.Term("print", Arrays.asList(
                new Ast.Term("",Arrays.asList(new Ast.StringLiteral("")))
        ));
        test(input, emptyast);
    }

    @Test
    void testExample10() {
        String input = "\"new\\\b\\\t\\\n\\\r\\\\line\"";
        List<Ast> expected = Arrays.asList(new Ast.StringLiteral("new\\\b\\\t\\\n\\\r\\\\line"));
        test2(input, expected);
    }

    @Test
    void testExampleWeirdString() {
        String input = "\"new\b\t\n\r\\line\"";
        List<Ast> expected = Arrays.asList(new Ast.StringLiteral("new\\\b\\\t\\\n\\\r\\\\line"));
        test2(input, expected);
    }

    @Test
    void testExample11() {
        String input = "%";
        List<Ast> expected = Arrays.asList(new Ast.Identifier("%"));
        test2(input, expected);
    }


    @Test
    void testExample12() {
        String input = "(";
        List<Ast> expected = Arrays.asList(new Ast.Identifier("("));
        test2(input, expected);
    }

    @Test
    void testExample13() {
        List<Ast> empty = new ArrayList<>();
        String input = "[print (f x))";
        Ast emptyast = new Ast.Term("print", Arrays.asList(
                new Ast.Term("x",Arrays.asList(new Ast.Identifier("x")))
        ));
        test(input, emptyast);
    }

    @Test
    void testExample14() {
        String input = "\"\"";
        List<Ast> expected = Arrays.asList(new Ast.StringLiteral(""));
        test2(input, expected);
    }

    @Test
    void testExample15() {
        String input = "(print \"\b\n\r\t\'\"\")";
        Ast expected = new Ast.Term("print", Arrays.asList(
                new Ast.StringLiteral("\b\n\r\t\'\"")
        ));
        test(input, expected);
    }

    @Test
    void testExample16() {
        String input = "\"\"";
        Ast expected = new Ast.StringLiteral("");
        test(input, expected);
    }



    void test(String input, Ast expected) {
        Ast ast = new Ast.Term("source", Arrays.asList(expected));
        Assertions.assertEquals(ast, Parser.parse(input));
    }

    void test2(String input, List<Ast> myast)
    {
        Ast ast = new Ast.Term("source",myast);
        Assertions.assertEquals(ast,Parser.parse(input));
    }

}
