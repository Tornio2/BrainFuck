package Translator;

public class Token {
    public enum Type {
        VAR, PRINT, WHILE, END,
        IDENTIFIER, NUMBER,
        EQUALS, PLUS, MINUS, MULTIPLY, DIVIDE,
        GREATER_THAN, LESS_THAN, GREATER_EQUALS, LESS_EQUALS, EQUALS_EQUALS,
        LEFT_PAREN, RIGHT_PAREN,
        EOF
    }

    private final Type type;
    private final String value;

    public Token(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + (value.isEmpty() ? "" : ": " + value);
    }
}