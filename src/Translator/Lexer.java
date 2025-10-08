package Translator;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int position = 0;
    private int line = 1;

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char currentChar = input.charAt(position);

            if (Character.isWhitespace(currentChar)) {
                if (currentChar == '\n') {
                    line++;
                }
                position++;
                continue;
            }

            if (Character.isLetter(currentChar)) {
                StringBuilder sb = new StringBuilder();

                while (position < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
                    sb.append(input.charAt(position));
                    position++;
                }

                String word = sb.toString();
                switch (word) {
                    case "VAR":
                        tokens.add(new Token(Token.Type.VAR, word));
                        break;
                    case "PRINT":
                        tokens.add(new Token(Token.Type.PRINT, word));
                        break;
                    case "WHILE":
                        tokens.add(new Token(Token.Type.WHILE, word));
                        break;
                    case "END":
                        tokens.add(new Token(Token.Type.END, word));
                        break;
                    default:
                        tokens.add(new Token(Token.Type.IDENTIFIER, word));
                        break;
                }
                continue;
            }

            if (Character.isDigit(currentChar)) {
                StringBuilder sb = new StringBuilder();

                while (position < input.length() && Character.isDigit(input.charAt(position))) {
                    sb.append(input.charAt(position));
                    position++;
                }

                tokens.add(new Token(Token.Type.NUMBER, sb.toString()));
                continue;
            }

            switch (currentChar) {
                case '=':
                    position++;
                    if (position < input.length() && input.charAt(position) == '=') {
                        tokens.add(new Token(Token.Type.EQUALS_EQUALS, "=="));
                        position++;
                    } else {
                        tokens.add(new Token(Token.Type.EQUALS, "="));
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
                    position++;
                    if (position < input.length() && input.charAt(position) == '=') {
                        tokens.add(new Token(Token.Type.GREATER_EQUALS, ">="));
                        position++;
                    } else {
                        tokens.add(new Token(Token.Type.GREATER_THAN, ">"));
                    }
                    break;

                case '<':
                    position++;
                    if (position < input.length() && input.charAt(position) == '=') {
                        tokens.add(new Token(Token.Type.LESS_EQUALS, "<="));
                        position++;
                    } else {
                        tokens.add(new Token(Token.Type.LESS_THAN, "<"));
                    }
                    break;

                default:
                    throw new RuntimeException("Unexpected character: " + currentChar + " at line " + line);
            }
        }

        tokens.add(new Token(Token.Type.EOF, ""));
        return tokens;
    }
}