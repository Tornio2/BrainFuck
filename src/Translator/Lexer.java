package Translator;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private int position = 0;
    private final List<Token> tokens = new ArrayList<>();

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> tokenize() {
        while (position < source.length()) {
            char c = source.charAt(position);

            if (Character.isWhitespace(c)) {
                position++;
                continue;
            }

            if (Character.isLetter(c)) {
                lexIdentifier();
                continue;
            }

            if (Character.isDigit(c)) {
                lexNumber();
                continue;
            }

            switch (c) {
                case '=':
                    if (position + 1 < source.length() && source.charAt(position + 1) == '=') {
                        tokens.add(new Token(Token.Type.EQUALS_EQUALS, "=="));
                        position += 2;
                    } else {
                        tokens.add(new Token(Token.Type.EQUALS, "="));
                        position++;
                    }
                    break;
                case '+':
                    tokens.add(new Token(Token.Type.PLUS, "+"));
                    position++;
                    break;
                case '-':
                    tokens.add(new Token(Token.Type.MINUS, "-"));
                    position++;
                    break;
                case '*':
                    tokens.add(new Token(Token.Type.MULTIPLY, "*"));
                    position++;
                    break;
                case '/':
                    tokens.add(new Token(Token.Type.DIVIDE, "/"));
                    position++;
                    break;
                case '>':
                    if (position + 1 < source.length() && source.charAt(position + 1) == '=') {
                        tokens.add(new Token(Token.Type.GREATER_EQUALS, ">="));
                        position += 2;
                    } else {
                        tokens.add(new Token(Token.Type.GREATER_THAN, ">"));
                        position++;
                    }
                    break;
                case '<':
                    if (position + 1 < source.length() && source.charAt(position + 1) == '=') {
                        tokens.add(new Token(Token.Type.LESS_EQUALS, "<="));
                        position += 2;
                    } else {
                        tokens.add(new Token(Token.Type.LESS_THAN, "<"));
                        position++;
                    }
                    break;
                case '(':
                    tokens.add(new Token(Token.Type.LEFT_PAREN, "("));
                    position++;
                    break;
                case ')':
                    tokens.add(new Token(Token.Type.RIGHT_PAREN, ")"));
                    position++;
                    break;
                default:
                    throw new RuntimeException("Unexpected character: " + c);
            }
        }

        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }

    private void lexIdentifier() {
        StringBuilder builder = new StringBuilder();

        while (position < source.length() &&
                (Character.isLetterOrDigit(source.charAt(position)) || source.charAt(position) == '_')) {
            builder.append(source.charAt(position));
            position++;
        }

        String identifier = builder.toString();

        switch (identifier) {
            case "VAR":
                tokens.add(new Token(Token.Type.VAR, identifier));
                break;
            case "PRINT":
                tokens.add(new Token(Token.Type.PRINT, identifier));
                break;
            case "WHILE":
                tokens.add(new Token(Token.Type.WHILE, identifier));
                break;
            case "END":
                tokens.add(new Token(Token.Type.END, identifier));
                break;
            default:
                tokens.add(new Token(Token.Type.IDENTIFIER, identifier));
                break;
        }
    }

    private void lexNumber() {
        StringBuilder builder = new StringBuilder();

        while (position < source.length() && Character.isDigit(source.charAt(position))) {
            builder.append(source.charAt(position));
            position++;
        }

        tokens.add(new Token(Token.Type.NUMBER, builder.toString()));
    }
}