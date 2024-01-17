package plc.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the tokens and returns the parsed AST.
     */
    public static Ast parse(List<Token> tokens) throws ParseException {
        return new Parser(tokens).parseSource();
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException {
        List<Ast.Statement> statements = new ArrayList<>();
        Ast.Source source = new Ast.Source(statements);
        while (tokens.index < tokens.tokens.size()) {
            source.getStatements().add(parseStatement());
        }
        return source;
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, assignment, if, or while
     * statement, then it is an expression statement. See these methods for
     * clarification on what starts each type of statement.
     */
    public Ast.Statement parseStatement() throws ParseException {
        if (peek(Token.Type.IDENTIFIER)) {
            Token node = tokens.get(0);
            if (node.getLiteral().equals("LET")) {
                return parseDeclarationStatement();
            } else if (node.getLiteral().equals("IF")) {
                return parseIfStatement();
            } else if (node.getLiteral().equals("WHILE")) {
                return parseWhileStatement();
            } else if (peek(Token.Type.IDENTIFIER, Token.Type.OPERATOR) && peek(Token.Type.IDENTIFIER, "=")) {
                return parseAssignmentStatement();
            }
            return parseExpressionStatement();
        } else {
            return parseExpressionStatement();
        }
    }

    /**
     * Parses the {@code expression-statement} rule. This method is called if
     * the next tokens do not start another statement type, as explained in the
     * javadocs of {@link #parseStatement()}.
     */
    public Ast.Statement.Expression parseExpressionStatement() throws ParseException {
        Ast.Statement.Expression retVal = new Ast.Statement.Expression(parseExpression());
        if (tokens.has(0) && tokens.get(0).getLiteral().equals(";")) {
            tokens.advance();
        } else {
            throw new ParseException("Could not find semicolon", tokens.index);
        }
        return retVal;
    }

    /**
     * Parses the {@code declaration-statement} rule. This method should only be
     * called if the next tokens start a declaration statement, aka {@code let}.
     */
    public Ast.Statement.Declaration parseDeclarationStatement() throws ParseException {
        tokens.advance();
        if (!peek(Token.Type.IDENTIFIER, ":", Token.Type.IDENTIFIER)) {
            throw new ParseException("Was not able to peek identifier, colon, identifier", tokens.index);
        }

        String identifier1 = tokens.get(0).getLiteral();
        tokens.advance();
        tokens.advance(); //skip colon
        String identifier2 = tokens.get(0).getLiteral();
        tokens.advance();
        Optional<Ast.Expression> val;

        if (tokens.has(0) && peek(Token.Type.OPERATOR) && tokens.get(0).getLiteral().equals("=")) {
            tokens.advance();
            if (tokens.has(0) && !tokens.get(0).getLiteral().equals(";")) {
                val = Optional.of(parseExpression());
            }else {
                throw new ParseException("Parse Exception undefined", tokens.index);
            }
        } else if (tokens.has(0) && tokens.get(0).getLiteral().equals(";")){
            val = Optional.empty();
        } else {
            throw new ParseException("Declaration statement not defined", tokens.index);
        }
        if (tokens.has(0) && tokens.get(0).getLiteral().equals(";")) {
            tokens.advance();
        } else {
            throw new ParseException("Could not find semicolon", tokens.index);
        }
        return new Ast.Statement.Declaration(identifier1, identifier2, val);
        //TODO Marc
    }

    /**
     * Parses the {@code assignment-statement} rule. This method should only be
     * called if the next tokens start an assignment statement, aka both an
     * {@code identifier} followed by {@code =}.
     */
    public Ast.Statement.Assignment parseAssignmentStatement() throws ParseException {
        //check for identifier then check for =
        //create new Assignment statement and set var equal to experession junk
        String ident = tokens.get(0).getLiteral();
        tokens.advance();
        if(!peek("="))
        {
            throw new ParseException("No equal in assignment statement",0);
        }else {
            tokens.advance();
        }
        Ast.Statement.Assignment ret = new Ast.Statement.Assignment(ident,parseExpression());
        if (tokens.has(0) && tokens.get(0).getLiteral().equals(";")) {
            tokens.advance();
        } else {
            throw new ParseException("Could not find semicolon", tokens.index);
        }
        return ret;
        // throw new UnsupportedOperationException(); //TODO

    }

    /**
     * Parses the {@code if-statement} rule. This method should only be called
     * if the next tokens start an if statement, aka {@code if}.
     */
    public Ast.Statement.If parseIfStatement() throws ParseException {
        tokens.advance();
        Ast.Expression expval = parseExpression();
        if (tokens.has(0) && peek(Token.Type.IDENTIFIER) && tokens.get(0).getLiteral().equals("THEN")) {
            tokens.advance();
        }else {
            throw new ParseException("THEN identifier does not exist", tokens.index);
        }
        List<Ast.Statement> thenStatements = new ArrayList<>();
        if(tokens.has(0)){
            while (tokens.has(0) && !((peek(Token.Type.IDENTIFIER) && peek("END")) || (peek(Token.Type.IDENTIFIER) && peek("ELSE")))) {
                thenStatements.add(parseStatement());
            }
        }else{throw new ParseException("THEN statement not found", tokens.index);}
        List<Ast.Statement> elseStatements = new ArrayList<>();
        if (tokens.has(0) && peek(Token.Type.IDENTIFIER) && peek("END")) {
            tokens.advance();
        } else if(tokens.has(0) && peek(Token.Type.IDENTIFIER) && peek("ELSE")){
            tokens.advance();
            if(tokens.has(0)){
                while(tokens.has(0) && !(peek(Token.Type.IDENTIFIER) && peek("END"))) {
                    elseStatements.add(parseStatement());
                }
            }else{throw new ParseException("THEN statement not found", tokens.index);}
            if (tokens.has(0) && peek(Token.Type.IDENTIFIER) && peek("END")) {
                tokens.advance();
            } else {throw new ParseException("END identifier not found", tokens.index);}
        } else {
            throw new ParseException("END or ELSE identifier not found", tokens.index);
        }
        return new Ast.Statement.If(expval, thenStatements, elseStatements);
    }

    /**
     * Parses the {@code while-statement} rule. This method should only be
     * called if the next tokens start a while statement, aka {@code while}.
     */
    public Ast.Statement.While parseWhileStatement() throws ParseException {
        tokens.advance();
        Ast.Expression expval = parseExpression();
        if (tokens.has(0) && peek(Token.Type.IDENTIFIER) && tokens.get(0).getLiteral().equals("DO")) {
            tokens.advance();
        }else {
            throw new ParseException("DO identifier does not exist", tokens.index);
        }
        List<Ast.Statement> whileStatements = new ArrayList<>();
        while (tokens.index < tokens.tokens.size() && !(peek("END") && peek(Token.Type.IDENTIFIER))) {
            whileStatements.add(parseStatement());
        }
        if (tokens.has(0) && peek(Token.Type.IDENTIFIER) && tokens.get(0).getLiteral().equals("END")) {
            tokens.advance();
        } else {
            throw new ParseException("END identifier not found", tokens.index);
        }
        return new Ast.Statement.While(expval, whileStatements);
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expression parseExpression() throws ParseException {
        return parseEqualityExpression();
        //TODO marc
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expression parseEqualityExpression() throws ParseException {
        Ast.Expression leftExpression = parseAdditiveExpression();
        if ((peek("==") || peek("!=")) && peek(Token.Type.OPERATOR)) {
            while ((peek("==") || peek("!=")) && peek(Token.Type.OPERATOR)) {
                String tokenOperator = tokens.get(0).getLiteral();
                tokens.advance();
                Ast.Expression rightExpression = parseAdditiveExpression();
                Ast.Expression.Binary bin = new Ast.Expression.Binary(tokenOperator, leftExpression, rightExpression);
                leftExpression = bin;
            }
        }
        return leftExpression;
        //TODO marc
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expression parseAdditiveExpression() throws ParseException {
        Ast.Expression left = parseMultiplicativeExpression();
        //        Ast.Expression right = new Ast.Expression();
        String operator="";
        //        tokens.advance();
        if(tokens.has(0)&&(peek("+")||peek("-")) && peek(Token.Type.OPERATOR))
        {
            while(peek("+")||peek("-"))
            {
                operator=tokens.get(0).getLiteral();
                tokens.advance();
                Ast.Expression right = parseMultiplicativeExpression();
                Ast.Expression.Binary bin = new Ast.Expression.Binary(operator, left, right);
                left = bin;
            }
        }
        return left;
        //throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expression parseMultiplicativeExpression() throws ParseException {
        Ast.Expression left = parsePrimaryExpression();
        String operator="";
        if(tokens.has(0)&&(peek("*")||peek("/")) && peek(Token.Type.OPERATOR))
        {
            while(peek("*")||peek("/"))
            {
                operator=tokens.get(0).getLiteral();
                tokens.advance();
                Ast.Expression right = parsePrimaryExpression();
                Ast.Expression.Binary bin = new Ast.Expression.Binary(operator, left, right);
                left = bin;
            }
        }
        return left;
    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */

    public Ast.Expression parsePrimaryExpression() throws ParseException {
        if ((peek("TRUE") | peek("FALSE")) && peek(Token.Type.IDENTIFIER)) {
            return parseBoolean();
        } else if ((peek(Token.Type.DECIMAL))) {
            return parseDecimal();
        } else if ((peek(Token.Type.INTEGER))) {
            return parseInteger();
        } else if (peek(Token.Type.STRING)) {
            return parseString();
        } else if (peek(Token.Type.OPERATOR) && peek("(")) {
            return parseGroup();
        } else if (peek(Token.Type.IDENTIFIER, Token.Type.OPERATOR) && peek(Token.Type.IDENTIFIER, "(")) {
            return parseFunction();
        } else if (peek(Token.Type.IDENTIFIER) && !peek(Token.Type.IDENTIFIER, "(")) {
            return parseVariable();
        }

        throw new ParseException("no match for expression", tokens.index); //TODO marc
    }

    public Ast.Expression.Group parseGroup() throws ParseException {
        tokens.advance();
        if (peek(Token.Type.OPERATOR) && peek(")")) {
            throw new ParseException("No expression in group", tokens.index);
        }
        Ast.Expression.Group groupStatement = new Ast.Expression.Group(parseExpression());
        if (peek(Token.Type.OPERATOR) && peek(")")) {
            tokens.advance();
        }else {
            throw new ParseException("No expression in group", tokens.index);
        }
        return groupStatement;
    }

    public Ast.Expression.Function parseFunction() throws ParseException {
        List<Ast.Expression> arguments = new ArrayList<>();
        String funcname = tokens.get(0).getLiteral();
        tokens.advance();
        tokens.advance();
        while (tokens.has(0) && !(peek(Token.Type.OPERATOR) && peek(")"))) {
            arguments.add(parseExpression());
            if (peek(",") && !peek(",", Token.Type.IDENTIFIER)) {
                throw new ParseException("No Expression After Comma", tokens.index);
            }
            if (peek(",")) {
                tokens.advance();
            }
            else if (!(peek(Token.Type.OPERATOR) && peek(")"))) {
                throw new ParseException("No commas peeked for argument expression", tokens.index);
            }
        }
        if (peek(Token.Type.OPERATOR) && peek(")")) {
            tokens.advance();
        } else {
            throw new ParseException("No closing parentheses found", tokens.index);
        }
        return new Ast.Expression.Function(funcname, arguments);
    }

    public Ast.Expression.Variable parseVariable() throws ParseException {
        Ast.Expression.Variable retVal = new Ast.Expression.Variable(tokens.get(0).getLiteral());
        tokens.advance();
        return retVal;
    }

    public Ast.Expression.Literal parseString() throws ParseException {
        String tokenString = tokens.get(0).getLiteral();
        Ast.Expression.Literal retVal = new Ast.Expression.Literal(tokenString.substring(1, tokenString.length()-1));
        tokens.advance();
        return retVal;
    }
    public Ast.Expression.Literal parseDecimal() throws ParseException {
        BigDecimal ret = new BigDecimal(tokens.get(0).getLiteral());
        Ast.Expression.Literal retVal = new Ast.Expression.Literal(ret);
        tokens.advance();
        return retVal;
    }
    public Ast.Expression.Literal parseBoolean() throws ParseException {
        Ast.Expression.Literal retVal;
        if (peek("TRUE")) {
            retVal = new Ast.Expression.Literal(Boolean.TRUE);
            tokens.advance();
            return retVal;
        }
        retVal = new Ast.Expression.Literal(Boolean.FALSE);
        tokens.advance();
        return retVal;
    }
    public Ast.Expression.Literal parseInteger() throws ParseException {
        BigInteger ret = new BigInteger(tokens.get(0).getLiteral());
        Ast.Expression.Literal retVal = new Ast.Expression.Literal(ret);
        tokens.advance();
        return retVal;
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
        for (int i = 0; i < patterns.length; i++) {
            if (!tokens.has(i)) {
                return false;
            } else if (patterns[i] instanceof Token.Type) {
                if (patterns[i] != tokens.get(i).getType()) {
                    return false;
                }
            } else if (patterns[i] instanceof String) {
                if (!patterns[i].equals(tokens.get(i).getLiteral())) {
                    return false;
                }
            } else {
                throw new AssertionError();
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if (peek) {
            for (int i = 0; i < patterns.length; i++) {
                tokens.advance();
            }
        }
        return peek;
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
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }

    }

}
