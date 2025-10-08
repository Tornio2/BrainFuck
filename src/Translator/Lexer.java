package Translator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "VAR|PRINT|WHILE|END|[a-zA-Z_][a-zA-Z0-9_]*|\\d+|\\+|\\-|\\*|/|>|<|=|==|!=|\\(|\\)|\\{|\\}|\\s+");

    private final String sourceCode;
    private final List<Token> tokens = new ArrayList<>();
    private int position = 0;

    public Lexer(String sourceCode) {
        this.sourceCode = sourceCode;
        tokenize();
    }

    private void tokenize() {
        Matcher matcher = TOKEN_PATTERN.matcher(sourceCode);

        while (matcher.find()) {
            String value = matcher.group().trim();
            if (value.isEmpty()) continue;

            TokenType type = determineType(value);
            if (type != TokenType.WHITESPACE) {
                tokens.add(new Token(type, value));
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
    }

    private TokenType determineType(String value) {
        switch (value) {
            case "VAR": return TokenType.VAR;
            case "PRINT": return TokenType.PRINT;
            case "WHILE": return TokenType.WHILE;
            case "END": return TokenType.END;
            case "+": return TokenType.PLUS;
            case "-": return TokenType.MINUS;
            case ">": return TokenType.GREATER_THAN;
            case "<": return TokenType.LESS_THAN;
            case "=": return TokenType.ASSIGN;
            case "==": return TokenType.EQUALS;
        }

        if (value.matches("\\d+")) {
            return TokenType.NUMBER;
        }

        if (value.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return TokenType.IDENTIFIER;
        }

        if (value.matches("\\s+")) {
            return TokenType.WHITESPACE;
        }

        return TokenType.UNKNOWN;
    }

    public Token getNextToken() {
        if (position >= tokens.size()) {
            return new Token(TokenType.EOF, "");
        }
        return tokens.get(position++);
    }

    public Token peekToken() {
        if (position >= tokens.size()) {
            return new Token(TokenType.EOF, "");
        }
        return tokens.get(position);
    }

    public void reset() {
        position = 0;
    }
}