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

            // Check if next token is an operator
            if (position < tokens.size() &&
                    (tokens.get(position).getType() == Token.Type.PLUS ||
                            tokens.get(position).getType() == Token.Type.MINUS)) {
                // This is an expression like x + y
                position--; // Go back to the identifier for parseExpression()
                value = parseExpression();
            } else {
                value = generateVariableAccess(rhsVarName);
            }
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

            // Check if next token is an operator
            if (position < tokens.size() &&
                    (tokens.get(position).getType() == Token.Type.PLUS ||
                            tokens.get(position).getType() == Token.Type.MINUS)) {
                // This is an expression like sum = sum - 1
                position--; // Go back to the identifier for parseExpression()
                value = parseExpression();
            } else {
                value = generateVariableAccess(rhsVarName);
            }
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

        // Get variable name for condition
        String varName = consume(Token.Type.IDENTIFIER).getValue();
        if (!variables.containsKey(varName)) {
            throw new RuntimeException("Variable not declared: " + varName);
        }

        // Get comparison operator
        Token.Type operatorType = tokens.get(position).getType();
        if (operatorType != Token.Type.GREATER_THAN &&
                operatorType != Token.Type.LESS_THAN &&
                operatorType != Token.Type.GREATER_EQUALS &&
                operatorType != Token.Type.LESS_EQUALS &&
                operatorType != Token.Type.EQUALS_EQUALS) {
            throw new RuntimeException("Expected comparison operator, got: " + operatorType);
        }
        position++; // Consume the operator

        // Get right side of comparison
        int value;
        if (tokens.get(position).getType() == Token.Type.NUMBER) {
            value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());
        } else if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            String rhsVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(rhsVarName)) {
                throw new RuntimeException("Variable not declared: " + rhsVarName);
            }
            // This is simplified - we would need more complex code to compare two variables
            throw new RuntimeException("Comparing to variables not yet supported");
        } else {
            throw new RuntimeException("Expected number or variable after comparison operator");
        }

        // Generate condition setup code (before the loop)
        String conditionSetup = generateConditionSetup(varName, operatorType, value);

        StringBuilder whileCode = new StringBuilder();
        whileCode.append(conditionSetup);
        whileCode.append("["); // Start the loop

        // Parse the body of the while loop
        while (position < tokens.size() && tokens.get(position).getType() != Token.Type.END) {
            whileCode.append(parseStatement());
        }

        consume(Token.Type.END);

        // Generate code to re-check the condition at the end of each iteration
        whileCode.append(generateConditionSetup(varName, operatorType, value));
        whileCode.append("]"); // End the loop

        return whileCode.toString();
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

        // First operand
        String firstVarName = null;
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            firstVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(firstVarName)) {
                throw new RuntimeException("Variable not declared: " + firstVarName);
            }
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            int value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());
            expCode.append(generateTempValue(value));
            firstVarName = null; // Not a variable
        } else {
            throw new RuntimeException("Expected identifier or number at start of expression");
        }

        // Operator
        Token.Type operatorType = tokens.get(position).getType();
        if (operatorType != Token.Type.PLUS && operatorType != Token.Type.MINUS) {
            throw new RuntimeException("Expected operator, got: " + operatorType);
        }
        position++; // Consume the operator

        // Second operand
        if (tokens.get(position).getType() == Token.Type.IDENTIFIER) {
            String secondVarName = consume(Token.Type.IDENTIFIER).getValue();
            if (!variables.containsKey(secondVarName)) {
                throw new RuntimeException("Variable not declared: " + secondVarName);
            }

            // Generate code for the operation
            if (firstVarName != null) {
                // Both operands are variables
                expCode.append(generateOperation(firstVarName, operatorType, secondVarName));
            } else {
                // First operand was a number (already in temp cell)
                expCode.append(generateOperationWithTemp(operatorType, secondVarName));
            }
        } else if (tokens.get(position).getType() == Token.Type.NUMBER) {
            int value = Integer.parseInt(consume(Token.Type.NUMBER).getValue());

            if (firstVarName != null) {
                // Variable operation with constant
                expCode.append(generateOperation(firstVarName, operatorType, value));
            } else {
                // Constant operation with constant (already have first value in temp)
                if (operatorType == Token.Type.PLUS) {
                    expCode.append("+".repeat(value));
                } else { // MINUS
                    expCode.append("-".repeat(value));
                }
            }
        } else {
            throw new RuntimeException("Expected identifier or number as second operand");
        }

        return expCode.toString();
    }

    private Token consume(Token.Type expectedType) {
        if (position >= tokens.size()) {
            throw new RuntimeException("Unexpected end of input, expected: " + expectedType);
        }

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

        // Set the value (use the provided value code)
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
        // Go to the variable's value cell and copy its value to a temporary location
        StringBuilder code = new StringBuilder();

        // We'll use the cell after the last variable as a temporary cell
        int tempCellPos = nextVarAddress;

        // First clear the temp cell
        code.append(">".repeat(tempCellPos));
        code.append("[-]");

        // Move to the variable's value
        code.append("<".repeat(tempCellPos - (variables.get(varName) + 1)));

        // Copy the value to the temp cell (preserving the original)
        code.append("[>".repeat(tempCellPos - (variables.get(varName) + 1)));
        code.append("+");
        code.append("<".repeat(tempCellPos - (variables.get(varName) + 1)));
        code.append("-]");

        // Move back to temp cell that now has the value
        code.append(">".repeat(tempCellPos - (variables.get(varName) + 1)));

        return code.toString();
    }

    private String generateTempValue(int value) {
        // Use a temporary cell to store a value
        StringBuilder code = new StringBuilder();
        int tempCellPos = nextVarAddress;

        // Go to temp cell
        code.append(">".repeat(tempCellPos));

        // Clear and set value
        code.append("[-]");
        code.append("+".repeat(value));

        return code.toString();
    }

    private String generateNumber(int value) {
        // Generate code to set the current cell to a specific value
        return "[-]" + "+".repeat(value);
    }

    private String generatePrint(String varName) {
        // Go to the variable's value cell
        StringBuilder code = new StringBuilder();
        code.append(">".repeat(variables.get(varName) + 1));

        // Copy the value to a temporary working cell
        code.append("[->+>+<<]");  // Copy to two cells ahead
        code.append(">>[-<<+>>]<<"); // Move one copy back to original

        // Add 48 to convert numeric value to ASCII ('0' is 48, '1' is 49, etc.)
        code.append(">"); // Move to the temporary cell
        code.append("+".repeat(48)); // Add 48 to get ASCII digit

        // Print the ASCII value
        code.append(".");

        // Clean up the temporary cell
        code.append("[-]");

        // Return to the start
        code.append("<");
        code.append("<".repeat(variables.get(varName) + 1));

        return code.toString();
    }

    private String generateOperation(String leftVar, Token.Type operatorType, String rightVar) {
        StringBuilder code = new StringBuilder();

        // Go to the left variable's value cell
        code.append(">".repeat(variables.get(leftVar) + 1));

        // Store the result in this cell
        // First clear it
        code.append("[-]");

        // Copy the left var value here
        code.append(">".repeat(variables.get(leftVar) + 1));
        code.append("[<+>-]");

        // Now apply the operation with the right variable
        code.append(">".repeat(variables.get(rightVar) - variables.get(leftVar)));

        switch (operatorType) {
            case PLUS:
                code.append("[<+>-]");
                break;
            case MINUS:
                code.append("[<->-]");
                break;
            default:
                throw new RuntimeException("Unsupported operator: " + operatorType);
        }

        // Return to the start
        code.append("<".repeat(variables.get(rightVar) + 1));

        return code.toString();
    }

    private String generateOperation(String leftVar, Token.Type operatorType, int value) {
        StringBuilder code = new StringBuilder();

        // Go to the left variable's value cell
        code.append(">".repeat(variables.get(leftVar) + 1));

        // Apply the operation directly
        switch (operatorType) {
            case PLUS:
                code.append("+".repeat(value));
                break;
            case MINUS:
                code.append("-".repeat(value));
                break;
            default:
                throw new RuntimeException("Unsupported operator: " + operatorType);
        }

        // Return to the start
        code.append("<".repeat(variables.get(leftVar) + 1));

        return code.toString();
    }

    private String generateOperationWithTemp(Token.Type operatorType, String rightVar) {
        StringBuilder code = new StringBuilder();

        // Temp value is already in the temp cell (nextVarAddress)
        // Need to apply operation with rightVar
        int tempPos = nextVarAddress;

        // Move to the right variable
        code.append("<".repeat(tempPos - (variables.get(rightVar) + 1)));

        // Create a copy of the right var that we can consume
        int tempCopyPos = nextVarAddress + 1;
        code.append("[>".repeat(tempCopyPos - (variables.get(rightVar) + 1)));
        code.append("+");
        code.append("<".repeat(tempCopyPos - (variables.get(rightVar) + 1)));
        code.append("-]");

        // Move to temp copy
        code.append(">".repeat(tempCopyPos - (variables.get(rightVar) + 1)));

        // Apply operation between temp and temp copy
        code.append("<"); // Move to temp

        switch (operatorType) {
            case PLUS:
                code.append(">[<+>-]<");
                break;
            case MINUS:
                code.append(">[<->-]<");
                break;
            default:
                throw new RuntimeException("Unsupported operator: " + operatorType);
        }

        // Result is now in the temp cell

        return code.toString();
    }

    private String generateConditionSetup(String varName, Token.Type operatorType, int value) {
        StringBuilder code = new StringBuilder();

        // Go to the variable's value cell
        code.append(">".repeat(variables.get(varName) + 1));

        // Create a copy of the variable's value in a temporary cell
        int tempCell = nextVarAddress;
        code.append("[-]"); // Clear original value first for simplicity

        // Now implement the condition logic
        switch (operatorType) {
            case GREATER_THAN: // x > value
                // Set the cell to 1 if x > value, 0 otherwise
                code.append(generateNumber(value + 1)); // Set to value + 1

                // Get the actual variable value
                int varCell = variables.get(varName) + 1;
                code.append(">".repeat(tempCell - varCell));
                code.append("[-]"); // Clear temp cell
                code.append("<".repeat(tempCell - varCell));

                // Copy variable value to temp cell
                code.append("[>+<-]>"); // Now at temp cell with variable value

                // Subtract value+1 from it
                code.append("<".repeat(tempCell - varCell)); // Back to var cell with value+1

                // Now we use the temp cell to decrement the var cell
                code.append("[>-<-]");

                // If result in temp is positive, var > value
                code.append(">"); // Go to temp

                // If temp is positive, set the original cell to 1, otherwise 0
                code.append("["); // If temp > 0
                code.append("<[-]+>-"); // Set var cell to 1, decrement temp
                code.append("]");

                // Return to var cell, which now contains 1 if x > value, 0 otherwise
                code.append("<");
                break;

            case LESS_THAN: // x < value
                // Similar approach for less than
                code.append(generateNumber(0)); // Start with 0

                // Go to temp cell
                code.append(">".repeat(tempCell - (variables.get(varName) + 1)));
                code.append("[-]"); // Clear temp

                // Set temp to value
                code.append("+".repeat(value));

                // Back to var cell
                code.append("<".repeat(tempCell - (variables.get(varName) + 1)));

                // Copy var to another temp
                int tempCell2 = tempCell + 1;
                code.append("[>".repeat(tempCell2 - (variables.get(varName) + 1)));
                code.append("+");
                code.append("<".repeat(tempCell2 - (variables.get(varName) + 1)));
                code.append("-]");

                // Now compare: if temp (value) > temp2 (var), then var < value
                code.append(">".repeat(tempCell - (variables.get(varName) + 1))); // Go to temp

                // Subtract temp2 from temp
                code.append("[>-<-]>");

                // If temp is positive, set var cell to 1
                code.append("[<[-]+>-]<");
                break;

            case EQUALS_EQUALS: // x == value
                // Set to 1 if equal, 0 otherwise
                // Copy var value to temp
                code.append("[>".repeat(tempCell - (variables.get(varName) + 1)));
                code.append("+");
                code.append("<".repeat(tempCell - (variables.get(varName) + 1)));
                code.append("-]");

                // Set original cell to 1 (assume equal)
                code.append("+");

                // Go to temp
                code.append(">".repeat(tempCell - (variables.get(varName) + 1)));

                // Subtract value from temp
                code.append("[-]" + "+".repeat(value));
                code.append("[<->-]");

                // If temp is not 0, set var cell to 0 (not equal)
                code.append("<");
                break;

            default:
                throw new RuntimeException("Unsupported condition operator: " + operatorType);
        }

        // Return to the start
        code.append("<".repeat(variables.get(varName)));

        return code.toString();
    }
}