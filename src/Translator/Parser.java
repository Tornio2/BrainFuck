package Translator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private final Lexer lexer;
    private List<Token> tokens;
    private int position = 0;
    private final Map<String, Integer> variables = new HashMap<>();
    private int nextVarAddress = 0;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public String parse() {
        tokens = lexer.tokenize();
        StringBuilder brainfuckCode = new StringBuilder();

        // Initialize memory layout
        brainfuckCode.append(generateMemoryLayout());

        while (position < tokens.size() && tokens.get(position).getType() != Token.Type.EOF) {
            brainfuckCode.append(parseStatement());
        }

        return brainfuckCode.toString();
    }

    private String parseStatement() {
        Token token = tokens.get(position);

        switch (token.getType()) {
            case VAR:
                return parseVarDeclaration();
            case PRINT:
                return parsePrintStatement();
            case WHILE:
                return parseWhileStatement();
            case IDENTIFIER:
                return parseAssignment();
            default:
                throw new RuntimeException("Unexpected token: " + token);
        }
    }

    private String parseVarDeclaration() {
        // VAR x = 5
        consume(Token.Type.VAR);
        String varName = consume(Token.Type.IDENTIFIER).getValue();

        // Ensure variable name is one character for now as per requirement
        if (varName.length() > 1) {
            throw new RuntimeException("Variable names must be one character long");
        }

        consume(Token.Type.EQUALS);

        String value;
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            // VAR z = x
            String rhsVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(rhsVarName)) {
                throw new RuntimeException("Variable not declared: " + rhsVarName);
            }
            value = generateVariableAccess(rhsVarName);
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            // VAR x = 5
            value = generateNumber(Integer.parseInt(consume(Token.Type.NUMBER).getValue()));
        } else {
            // VAR sum = x + y
            value = parseExpression();
        }

        // Store the variable address for future reference
        variables.put(varName, nextVarAddress);
        nextVarAddress += 2; // Each variable takes 2 cells (name + value)

        return generateVariableDeclaration(varName, value);
    }

    private String parseAssignment() {
        // sum = sum - 1
        String varName = consume(Token.Type.IDENTIFIER).getValue();

        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable not declared: " + varName);
        }

        consume(Token.Type.EQUALS);

        String value;
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            // z = x
            String rhsVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(rhsVarName)) {
                throw new RuntimeException("Variable not declared: " + rhsVarName);
            }
            value = generateVariableAccess(rhsVarName);
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            // x = 5
            value = generateNumber(Integer.parseInt(consume(Token.Type.NUMBER).getValue()));
        } else {
            // sum = sum - 1
            value = parseExpression();
        }

        return generateAssignment(varName, value);
    }

    private String parseWhileStatement() {
        // WHILE sum > 0
        consume(Token.Type.WHILE);
        String condition = parseCondition();

        StringBuilder whileCode = new StringBuilder();
        whileCode.append(condition);
        whileCode.append("[");

        while (tokens.get(position).getType() != Token.Type.END) {
            whileCode.append(parseStatement());
        }

        consume(Token.Type.END);
        whileCode.append(condition);
        whileCode.append("]");

        return whileCode.toString();
    }

    private String parseCondition() {
        // sum > 0
        String varName = consume(Token.Type.IDENTIFIER).getValue();

        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable not declared: " + varName);
        }

        Token.Type operatorType = tokens.get(position).getType();
        position++; // Consume the operator

        if (operatorType != Token.Type.GREATER_THAN &&
                operatorType != Token.Type.LESS_THAN &&
                operatorType != Token.Type.GREATER_EQUALS &&
                operatorType != Token.Type.LESS_EQUALS &&
                operatorType != Token.Type.EQUALS_EQUALS) {
            throw new RuntimeException("Expected comparison operator");
        }

        int value;
        if (tokens.get(position).getType() == Token.Type.NUMBER) {
            value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());
        } else if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            String rhsVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(rhsVarName)) {
                throw new RuntimeException("Variable not declared: " + rhsVarName);
            }
            // This is simplified - we would need more complex code to compare two variables
            // For now, we're assuming it's always compared to a number
            throw new RuntimeException("Comparing to variables not yet supported");
        } else {
            throw new RuntimeException("Expected number or variable after comparison operator");
        }

        return generateCondition(varName, operatorType, value);
    }

    private String parsePrintStatement() {
        // PRINT sum
        consume(Token.Type.PRINT);
        String varName = consume(Token.Type.IDENTIFIER).getValue();

        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable not declared: " + varName);
        }

        return generatePrint(varName);
    }

    private String parseExpression() {
        // x + y, sum - 1, etc.
        StringBuilder expCode = new StringBuilder();

        // Create a temporary cell for calculation
        expCode.append(">[-]<"); // Clear the temp cell

        // First operand
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            String varName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable not declared: " + varName);
            }
            expCode.append(generateVariableAccess(varName));
            expCode.append("[>+<-]"); // Copy to temp cell
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            int value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());
            expCode.append(generateNumber(value));
            expCode.append("[>+<-]"); // Copy to temp cell
        } else {
            throw new RuntimeException("Expected identifier or number");
        }

        // Operator
        Token.Type operatorType = tokens.get(position).getType();
        position++; // Consume the operator

        // Second operand
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            String varName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(varName)) {
                throw new RuntimeException("Variable not declared: " + varName);
            }

            switch (operatorType) {
                case PLUS:
                    expCode.append(generateVariableAccess(varName));
                    expCode.append("[>+<-]"); // Add to temp cell
                    break;
                case MINUS:
                    expCode.append(generateVariableAccess(varName));
                    expCode.append("[>-<-]"); // Subtract from temp cell
                    break;
                // Multiply and divide are more complex and omitted for brevity
                default:
                    throw new RuntimeException("Unsupported operator: " + operatorType);
            }
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            int value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());

            switch (operatorType) {
                case PLUS:
                    expCode.append(">"); // Move to temp cell
                    expCode.append("+".repeat(value)); // Add constant
                    expCode.append("<"); // Move back
                    break;
                case MINUS:
                    expCode.append(">"); // Move to temp cell
                    expCode.append("-".repeat(value)); // Subtract constant
                    expCode.append("<"); // Move back
                    break;
                // Multiply and divide are more complex and omitted for brevity
                default:
                    throw new RuntimeException("Unsupported operator: " + operatorType);
            }
        }

        // The result is now in the temp cell
        expCode.append(">");
        return expCode.toString();
    }

    private Token consume(Token.Type expectedType) {
        Token token = tokens.get(position);

        if (token.getType() != expectedType) {
            throw new RuntimeException("Expected " + expectedType + ", got " + token.getType() + ": " + token.getValue());
        }

        position++;
        return token;
    }

    // BRAINFUCK GENERATION CODE

    private String generateMemoryLayout() {
        // Set up initial memory layout
        // We'll use a simple layout where each variable takes 2 cells:
        // cell 1: variable name (ASCII value)
        // cell 2: variable value
        return "";
    }

    private String generateVariableDeclaration(String varName, String valueCode) {
        // Go to the variable's address
        StringBuilder code = new StringBuilder();
        code.append(">".repeat(variables.get(varName)));

        // Set the variable name (ASCII value)
        code.append("[-]"); // Clear the cell
        code.append("+".repeat((int)varName.charAt(0))); // Set to ASCII value

        // Move to value cell
        code.append(">");
        code.append("[-]"); // Clear the cell

        // Set the value
        code.append(valueCode);

        // Return to the start
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }

    private String generateAssignment(String varName, String valueCode) {
        // Go to the variable's value cell
        StringBuilder code = new StringBuilder();
        code.append(">".repeat(variables.get(varName) + 1));

        // Clear the cell
        code.append("[-]");

        // Set the value
        code.append(valueCode);

        // Return to the start
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }

    private String generateVariableAccess(String varName) {
        // Go to the variable's value cell
        StringBuilder code = new StringBuilder();
        code.append(">".repeat(variables.get(varName) + 1));

        // The value is now in the current cell
        // You can use it, but make sure to copy it if needed since operations will modify it

        // For now, we'll just return the value as-is
        // The calling function will need to handle copying if needed

        // Return to the start
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }

    private String generateNumber(int value) {
        // Generate code to set the current cell to a specific value
        return "+".repeat(value);
    }

    private String generatePrint(String varName) {
        // Go to the variable's value cell
        StringBuilder code = new StringBuilder();
        code.append(">".repeat(variables.get(varName) + 1));

        // Print the value as ASCII
        code.append(".");

        // Return to the start
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }

    private String generateCondition(String varName, Token.Type operatorType, int value) {
        StringBuilder code = new StringBuilder();

        // Go to the variable's value cell
        code.append(">".repeat(variables.get(varName) + 1));

        // For WHILE loops, we need the cell to be non-zero to enter the loop
        // and zero to exit

        switch (operatorType) {
            case GREATER_THAN:
                // For x > 0, we can just use the value directly
                // For x > n, we need to subtract n and check if the result is positive
                code.append(generateNumber(-value));
                code.append("[");  // This will be executed if x > n
                code.append("+");  // Set flag to 1
                code.append("]");
                break;
            case LESS_THAN:
                // For x < n, we need to use a more complex approach
                // This is simplified and might not work for all cases
                code.append("[-]"); // Clear the cell
                break;
            case EQUALS_EQUALS:
                // For x == n, we need to check if x - n == 0
                code.append(generateNumber(-value));
                // If it's zero, then x == n
                // This is a simplified approach
                break;
            default:
                throw new RuntimeException("Unsupported condition operator: " + operatorType);
        }

        // Return to the start
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }
}