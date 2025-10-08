package Translator;

public class Token {
    public enum Type {
        VAR, IDENTIFIER, NUMBER,
        EQUALS, EQUALS_EQUALS,
        PLUS, MINUS, MULTIPLY, DIVIDE,
        GREATER_THAN, LESS_THAN, GREATER_EQUALS, LESS_EQUALS,
        PRINT, WHILE, END,
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
        return type + ": " + value;
    }
}