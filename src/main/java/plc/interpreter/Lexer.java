package plc.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException}.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers, they're not necessary but their use will make the implementation a
 * lot easier. Regex isn't the most performant way to go but it gets the job
 * done, and the focus here is on the concept.
 */
public final class Lexer {

    final CharStream chars;
    Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Lexes the input and returns the list of tokens.
     */
    public static List<Token> lex(String input) throws ParseException {
        return new Lexer(input).lex();
    }

    /**
     * Repeatedly lexes the next token using {@link #lexToken()} until the end
     * of the input is reached, returning the list of tokens lexed. This should
     * also handle skipping whitespace.
     */
    List<Token> lex() throws ParseException {
        List<Token> tokens = new ArrayList<>();
        while (chars.has(0)){
            if (peek("\\s") || peek("[\n\r\t]")) {
                chars.advance();
                chars.reset();
            }
            else {
                tokens.add(lexToken());
                chars.reset();
            }
        }
        return tokens;
    }

    /**
     * Lexes the next token. It may be helpful to have this call other methods,
     * such as {@code lexIdentifier()} or {@code lexNumber()}, based on the next
     * character(s).
     *
     * Additionally, here is an example of lexing a character literal (not used
     * in this assignment) using the peek/match methods below.
     *
     * <pre>
     * {@code
     *     private plc.interpreter.Token lexCharacter() {
     *         if (!match("\'")) {
     *             //Your lexer should prevent this from happening, as it should
     *             // only try to lex a character literal if the next character
     *             // begins a character literal.
     *             //Additionally, the index being passed back is a 'ballpark'
     *             // value. If we were doing proper diagnostics, we would want
     *             // to provide a range covering the entire error. It's really
     *             // only for debugging / proof of concept.
     *             throw new ParseException("Next character does not begin a character literal.", chars.index);
     *         }
     *         if (!chars.has(0) || match("\'")) {
     *             throw new ParseException("Empty character literal.",  chars.index);
     *         } else if (match("\\")) {
     *             //lex escape characters...
     *         } else {
     *             chars.advance();
     *         }
     *         if (!match("\'")) {
     *             throw new ParseException("Unterminated character literal.", chars.index);
     *         }
     *         return chars.emit(Token.Type.CHARACTER);
     *     }
     * }
     * </pre>
     */
    Token lexToken() throws ParseException {
        // looks at start of each character
        if (peek("\\s")) {
            throw new ParseException("Unexpected character at ", chars.index);
        }
        if (peek("[0-9]") || peek("[+-]", "[0-9]")) {
            return lexNumber();
        }else if (peek("[A-Za-z_\\*/:!?<>=.+-]")) {
            return lexIdentifier();
        }else if (peek("\"")) {
            return lexString();
        }
        chars.advance();
        return chars.emit(Token.Type.OPERATOR);
    }
    Token lexIdentifier() {

        if (!peek("[A-Za-z_\\*/:!?<>=]") && !peek("\\.", "[0-9A-Za-z_+\\-*/:!?<>=]") && peek("\\+\\-", "[0-9]")) {
            throw new ParseException("Unexpected character for Identifier: ", chars.index);
        }
        if (peek("\\.") && !peek("\\.", "[0-9A-Za-z_+\\-*/:!?<>.=]")) {
            chars.advance();
            return chars.emit(Token.Type.OPERATOR);
        }
        else {
            while(peek("[0-9A-Za-z_+\\-*/:!?<>=.]")) {
                chars.advance();
            }
            return chars.emit(Token.Type.IDENTIFIER);
        }

    }

    Token lexNumber() {
        if (!peek("[0-9]") && !peek("[+-]")) {
            throw new ParseException("Unexpected character for Number: ", chars.index);
        }
        chars.advance(); // goes past first number or first (+ -)
        while (peek("[0-9]"))
            chars.advance();
        if (chars.has(0) && peek("\\.")) {
            chars.advance();
            if (peek("[0-9]")) {
                while (peek("[0-9]")) {
                    chars.advance();
                }
                return chars.emit(Token.Type.NUMBER);
            } else {
                throw new ParseException("Not a number: ", chars.index);
            }

        }
        return chars.emit(Token.Type.NUMBER);
    }

    Token lexString() throws ParseException {
        chars.advance();
        while (chars.has(0) && !peek("\"")) {
            if (peek("\\\\", "[^bnrt'\"\\\\]")) {
                throw new ParseException("Invalid Escape", chars.index);
            }
            else if (peek("\\\\", "[bnrt\'\"\\\\]")) {
                chars.advance();
            }
            chars.advance();
        }
        if (!chars.has(0) && (!chars.has(-1) || (chars.has(-1) && (chars.get(-1) != '\"')))) {
            throw new ParseException("Not a String", chars.index);
        }
        if (chars.has(0)) {
            chars.advance();
        }
        return chars.emit(Token.Type.STRING);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true for the sequence {@code 'a', 'b', 'c'}
     */
    boolean peek(String... patterns) {
        int offset = 0;
        for (String pattern : patterns) {
            Pattern regexPattern = Pattern.compile(pattern);
            if (!chars.has(offset) || !regexPattern.matcher(String.valueOf(chars.get(offset))).matches()) {
                return false;
            }
            offset += 1;
        }
        return true;
    }

    /**
     * Returns true in the same way as peek, but also advances the CharStream to
     * if the characters matched.
     */
    boolean match(String... patterns) {
        if (!peek(patterns)) {
            return false;
        }
        for (int i = 0; i < patterns.length; i++) {
            chars.advance();
        }
        return true;
    }

    /**
     * This is basically a sequence of characters. The index is used to maintain
     * where in the input string the lexer currently is, and the builder
     * accumulates characters into the literal value for the next token.
     */
    static final class CharStream {

        final String input;
        int index = 0;
        int length = 0;

        CharStream(String input) {
            this.input = input;
        }

        /**
         * Returns true if there is a character at index + offset.
         */
        boolean has(int offset) {

            return this.index + offset < this.input.length();
        }

        /**
         * Gets the character at index + offset.
         */
        char get(int offset) {
            if (index + offset >= this.input.length()) {
                throw new UnsupportedOperationException();
            }
            return this.input.charAt(index + offset);

        }

        /**
         * Advances to the next character, incrementing the current index and
         * length of the literal being built.
         */
        void advance() {
            this.index += 1;
            this.length += 1;
        }

        /**
         * Resets the length to zero, skipping any consumed characters.
         */
        void reset() {
            this.length = 0;
        }

        /**
         * Returns a token of the given type with the built literal and resets
         * the length to zero. The index of the token should be the
         * <em>starting</em> index.
         */
        Token emit(Token.Type type) {
            if (this.index > this.input.length()) {
                throw new UnsupportedOperationException();
            }
            return new Token(type, this.input.substring(this.index - this.length, this.index), this.index - this.length);
        }

    }

}
