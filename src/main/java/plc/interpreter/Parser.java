package plc.interpreter;

import javafx.scene.input.TouchEvent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    private Parser(String input) {

        tokens = new TokenStream(Lexer.lex(input));
    }

    /**
     * Parses the input and returns the AST
     */
    public static Ast parse(String input) {

        return new Parser(input).parse();
    }

    /**
     * Repeatedly parses a list of ASTs, returning the list as arguments of an
     * {@link Ast.Term} with the identifier {@code "source"}.
     */
    private Ast parse() {
        /*if(tokens.tokens.size()==0)
        {
            throw new ParseException("Empty Token List", 0);
        }*/
        List<Ast> list = new ArrayList<>();
        Ast.Term firstEntry = new Ast.Term("source",list);
        while(tokens.index < tokens.tokens.size()){
            firstEntry.getArgs().add(parseAst());
            if(!peek("(")&&!peek("[")){
                tokens.advance();
            }
            //tokens.advance();
        }

        return firstEntry;

        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses an AST from the given tokens based on the provided grammar. Like
     * the lexToken method, you may find it helpful to have this call other
     * methods like {@code parseTerm()}. In a recursive descent parser, each
     * rule in the grammar would correspond with a {@code parseX()} function.
     *
     * Additionally, here is an example of parsing a function call in a language
     * like Java, which has the form {@code name(args...)}.
     *
     * <pre>
     * {@code
     *     private Ast.FunctionExpr parseFunctionExpr() {
     *         //In a real parser this would be more complex, as the parser
     *         //wouldn't know this should be a function call until reaching the
     *         //opening parenthesis, like name(... <- here. You won't have this
     *         //problem in this project, but will for the compiler project.
     *         if (!match(Token.Type.IDENTIFIER)) {
     *             throw new ParseException("Expected the name of a function.");
     *         }
     *         String name = tokens.get(-1).getLiteral();
     *         if (!match("(")) {
     *             throw new ParseException("Expected opening bra
     *         }
     *         List<Ast> args = new ArrayList<>();
     *         while (!match(")")) {
     *             //recursive call to parseExpr(), not shown here
     *             args.add(parseExpr());
     *             //next token must be a closing parenthesis or comma
     *             if (!peek(")") && !match(",")) {
     *                 throw new ParseException("Expected closing parenthesis or comma after argument.", tokens.get(-1).getIndex());
     *             }
     *         }
     *         return new Ast.FunctionExpr(name, args);
     *     }
     * }
     * </pre>
     */
    private Ast parseAst() {

        if(peek("(")||peek("["))
        {
            tokens.advance();
            if (peek(Token.Type.IDENTIFIER))
            {
                return parseTerm(tokens.get(-1).getLiteral());
            }else{
                throw new ParseException("Expected Identifier", 0);
            }
        }
        else if(peek(Token.Type.IDENTIFIER))
        {
            return parseIdentifier();
        }
        else if(peek(Token.Type.NUMBER))
        {
            return parseNumberLiteral();
        }
        else if(peek(Token.Type.STRING))
        {
            return parseStringLiteral();
        }else{
            throw new ParseException("Character Not Allowed",0);
        }
    }

    private Ast.Identifier parseIdentifier(){
        String holder = tokens.get(0).getLiteral();
        Ast.Identifier ret = new Ast.Identifier(holder);
        return ret;
    }

    private Ast.Term parseTerm(String val){

        if (!peek(Token.Type.IDENTIFIER)) {
            throw new ParseException("Identifier expected", tokens.get(0).getIndex());
        }
        tokens.advance();
        List<Ast> ast = new ArrayList<>();
        Ast.Term ret = new Ast.Term(tokens.get(-1).getLiteral(), ast);
        while(tokens.index <= tokens.tokens.size())
        {
                if(match("[")||match("("))
                {
                    ast.add(parseTerm(tokens.get(-1).getLiteral()));
                    if(tokens.index == tokens.tokens.size())
                    {
                        //break;
                        throw new ParseException("No Closing Operator", 0);
                        //tokens.get(0).getIndex()
                    }
                    continue;
                }
                if(match("]")||match(")"))
                {
                    if((val.equals("(")&&tokens.get(-1).getLiteral().equals("]"))||(val.equals("[")&&tokens.get(-1).getLiteral().equals(")")))
                    {
                        throw new ParseException("Improper Closing Operator", 0);
                        //tokens.get(0).getIndex()
                    }
                    return ret;
                }
                ret.getArgs().add(parseAst());
                tokens.advance();
        }
        return ret;
    }

    private Ast.NumberLiteral parseNumberLiteral(){
        BigDecimal val = new BigDecimal(tokens.get(0).getLiteral());
        Ast.NumberLiteral ret = new Ast.NumberLiteral(val);
        return ret;
    }

    private Ast.StringLiteral parseStringLiteral(){

        String holder = tokens.get(0).getLiteral();
        holder = holder.substring(1, holder.length()-1);
        holder = holder.replace("\\\"", "\"");
        holder = holder.replace("\\n", "\n");
        holder = holder.replace("\\b", "\b");
        holder = holder.replace("\\r", "\r");
        holder = holder.replace("\\t", "\t");
        holder = holder.replace("\\'", "'");
        holder = holder.replace("\\\\", "\\");
        Ast.StringLiteral ret = new Ast.StringLiteral(holder);
        return ret;
    }



    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {

        int offset = 0;
        for (Object pattern : patterns) {

            if(pattern instanceof Token.Type)
            {
                if (!tokens.has(offset) || !tokens.get(offset).getType().equals(pattern)) {
                    return false;
                }
            }else if(pattern instanceof String){
                if (!tokens.has(offset) || !tokens.get(offset).getLiteral().equals(pattern)) {
                    return false;
                }

            }

            offset += 1;
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {

        if (!peek(patterns)) {
            return false;
        }
        for (int i = 0; i < patterns.length; i++) {
            tokens.advance();
        }
        return true;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {

            return this.index + offset < this.tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {

            if(index + offset >= this.tokens.size()){
                throw new UnsupportedOperationException();
            }
            return this.tokens.get(index+offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {

            this.index += 1;
        }

    }

}
