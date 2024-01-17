package plc.interpreter;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains JUnit tests for {@link Regex}. Tests declarations for steps 1 & 2
 * are provided, you must add your own for step 3.
 *
 * To run tests, either click the run icon on the left margin or execute the
 * gradle test task, which can be done by clicking the Gradle tab in the right
 * sidebar and navigating to Tasks > verification > test Regex(double click to run).
 */
public class RegexTests {

    /**
     * This is a parameterized test for the {@link Regex#EMAIL} regex. The
     * {@link ParameterizedTest} annotation defines this method as a
     * parameterized test, and {@link MethodSource} tells JUnit to look for the
     * static method {@link #testEmailRegex()}.
     *
     * For personal preference, I include a test name as the first parameter
     * which describes what that test should be testing - this is visible in
     * IntelliJ when running the tests.
     */
    @ParameterizedTest
    @MethodSource

    public void testEmailRegex(String test, String input, boolean success) {
        test(input, Regex.EMAIL, success);
    }

    /**
     * This is the factory method providing test cases for the parameterized
     * test above - note that it is static, takes no arguments, and has the same
     * name as the test. The {@link Arguments} object contains the arguments for
     * each test to be passed to the function above
     */
    public static Stream<Arguments> testEmailRegex() {
        return Stream.of(
                Arguments.of("Local Part Missing", "@domain.tld", false),
                Arguments.of("Local Part Numeric", "123@domain.tld", true),
                Arguments.of("Local Part Valid Symbols", "._-@domain.tld", true),
                Arguments.of("Local Part Invalid Characters", "#$%@domain.tld", false),
                Arguments.of("At Sign Missing", "localdomain.tld", false),
                Arguments.of("Domain Name Missing", "local@.tld", true),
                Arguments.of("Domain Name Numbers", "local@123.tld", true),
                Arguments.of("Domain Name Hyphen", "local@a-b-c.tld", true),
                Arguments.of("Domain Name No Underscore", "local@a_b_c.tld", false),
                Arguments.of("Domain Name Invalid Characters", "local@#$%.tld", false),
                Arguments.of("Domain Period Missing", "local@domaintld", false),
                Arguments.of("Domain Subdomains", "local@domain.sub.tld", false),
                Arguments.of("TLD Uppercase Characters", "local@domain.TLD", false),
                Arguments.of("TLD Invalid Characters", "local@domain.#$%", false),
                Arguments.of("TLD 0 Characters", "local@domain.", false),
                Arguments.of("TLD 1 Characters", "local@domain.a", false),
                Arguments.of("TLD 2 Characters", "local@domain.ab", true),
                Arguments.of("TLD 4+ Characters", "local@domain.abcd", false)
        );
    }


    @ParameterizedTest
    @MethodSource
    public void testFileNamesRegex(String test, String input, boolean success) {
        //this one is different as we're also testing the file name capture
        Matcher matcher = test(input, Regex.FILE_NAMES, success);
        if (success) {
            Assertions.assertEquals(input.substring(0, input.indexOf(".")), matcher.group("name"));
        }
    }

    public static Stream<Arguments> testFileNamesRegex() {
        return Stream.of(
                Arguments.of("Java File", "Regex.tar.java", true),
                Arguments.of("Java Class", "RegexTests.class", true),
                Arguments.of("Number File Test", "RegexTests12345.class", true),
                Arguments.of("Extension File Test", "Test.class.java", true),
                Arguments.of("one letter files", "a.1.2.3.4.class", true),
                Arguments.of("doc 1", "!@#$%^&*.java", true),
                Arguments.of("unicode name", "ρ★⚡.java", true),
                Arguments.of("unicode middle extension", "name.ρ★⚡.java", true),

                Arguments.of("Directory", "directory", false),
                Arguments.of("Incorrect File Order", "File.test.java.extension", false),
                Arguments.of("Symbol File Test", "Test$&*>#.classs", false),
                Arguments.of("No File Name Test", ".java", false),
                Arguments.of("Python File", "scrippy.py", false)
        );
    }


    @ParameterizedTest
    @MethodSource
    public void testEvenStringsRegex(String test, String input, boolean success) {
        test(input, Regex.EVEN_STRINGS, success);
    }

    public static Stream<Arguments> testEvenStringsRegex() {
        return Stream.of(
                Arguments.of("14 Characters", "thishas14chars", true),
                Arguments.of("10 Characters", "i<3pancakes!", true),
                Arguments.of("20 Characters", "i<3SpaghettiSoMuch!!", true),
                Arguments.of("Only Numbers", "12345678912345", true),
                Arguments.of("Mix Test", "1234ASDF^&*%", true),

                Arguments.of("6 Characters", "6chars", false),
                Arguments.of("22 Characters", "ThisTestShouldNotWork!", false),
                Arguments.of("13 Characters", "TestThirteen!", false),
                Arguments.of("Symbols", "<>?'[]+-&*()!@#$%", false),
                Arguments.of("15 Characters", "i<3pancakes!!", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIntegerListRegex(String test, String input, boolean success) {
        test(input, Regex.INTEGER_LIST, success);
    }

    public static Stream<Arguments> testIntegerListRegex() {
        return Stream.of(
                Arguments.of("Empty List", "[]", true),
                Arguments.of("Single Element", "[1]", true),
                Arguments.of("Multiple Elements", "[1,2,3]", true),
                Arguments.of("Large Number Test", "[100000,2345678,3333333333]", true),
                Arguments.of("Space Test", "[1, 2, 3]", true),


                Arguments.of("Missing Brackets", "1,2,3", false),
                Arguments.of("Missing Commas", "[1 2 3]", false),
                Arguments.of("Missing Commas", "[A,B,C]", false),
                Arguments.of("Missing Commas", "[$,%,&]", false),
                Arguments.of("Trailing Comma", "[1,2,3,]", false),
                Arguments.of("Space after Num", "[1 ,2 ,3 ]", false),
                Arguments.of("zero", "[0]", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testIdentifierRegex(String test, String input, boolean success) {
        test(input, Regex.IDENTIFIER, success);
    }

    public static Stream<Arguments> testIdentifierRegex() {
        return Stream.of(
                Arguments.of("Normal Text Test", "getName", true),
                Arguments.of("Text And Symbols Test", "is-empty?", true),
                Arguments.of("Symbols Test", "<=>", true),
                Arguments.of("More Symbols Test", "+-*/.:!?<>=", true),
                Arguments.of("Number Test", "-123456789", true),
                Arguments.of("Single letter", "c", true),
                Arguments.of("Single symbol", "/", true),

                Arguments.of("Digit Beginning Test", "42=life", false),
                Arguments.of("Comma Test", "why,are,there,commas,", false),
                Arguments.of("Empty String", "", false),
                Arguments.of("period", ".", false),
                Arguments.of("Illegal Characters", "(as2$6_ +fk)", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testNumberRegex(String test, String input, boolean success) {
        test(input, Regex.NUMBER, success);
    }

    public static Stream<Arguments> testNumberRegex() {
        return Stream.of(
                Arguments.of("Regular Number Test", "1", true),
                Arguments.of("Negative Number Test", "-1.0", true),
                Arguments.of("Positive Number Test", "+1.0", true),
                Arguments.of("Leading And Trailing Zeroes Test", "007.000", true),
                Arguments.of("0's Number Test", "00000.000000", true),

                Arguments.of("No Trailing Zeroes Test", "1.", false),
                Arguments.of("No Leading Zeroes Test", ".5", false),
                Arguments.of("empty string", "", false),
                Arguments.of("Multiple dots", "2.3.4", false),
                Arguments.of("Both positive and negative", "-+123" ,false),
                Arguments.of("Letters and Symbols Test", "ABCD$%^&", false)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void testStringRegex(String test, String input, boolean success) {
        test(input, Regex.STRING, success);
    }

    public static Stream<Arguments> testStringRegex() {
        return Stream.of(
                Arguments.of("Empty String Test", "\"\"", true),
                Arguments.of("Normal String Test", "\"abc\"", true),
                Arguments.of("b String Test", "\"Hello,\bWorld!\"", true),
                Arguments.of("New Line Test", "\"Hello,\nWorld!\"", true),
                Arguments.of("r String Test", "\"Hello,\rWorld!\"", true),
                Arguments.of("t String Test", "\"Hello,\tWorld!\"", true),
                Arguments.of("' String Test", "\"Hello,\'World!\"", true),
                Arguments.of("Normal String Test", "\"abc\n\b\r\t\"", true),
                Arguments.of("Illegal Form Feed Test", "\"\f\f\"", true),

                Arguments.of("Missing Bracket Test", "\"unterminated", false),
                Arguments.of("Incorrect Escape Test", "\"invalid\\escape\"", false),
                Arguments.of("Missing Bracket Test", "Test\"ABCD\"Test", false),
                Arguments.of("Missing Bracket Test", "No quotes", false),
                Arguments.of("Middle quotes", "\"quote\"quote\"", false)

        );
    }

    /**
     * Asserts that the input matches the given pattern and returns the matcher
     * for additional assertions.
     */
    private static Matcher test(String input, Pattern pattern, boolean success) {
        Matcher matcher = pattern.matcher(input);
        Assertions.assertEquals(success, matcher.matches());
        return matcher;
    }

}
