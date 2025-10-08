package Translator;

public class CodeGenerator {
    private final SymbolTable symbolTable = new SymbolTable();
    private final StringBuilder code = new StringBuilder();
    private int tempCellPosition;

    public String generate(AST ast) {
        if (ast instanceof ProgramNode) {
            generateProgram((ProgramNode) ast);
        }
        return code.toString();
    }

    private void generateProgram(ProgramNode program) {
        // Initialize temp cell position after all variables
        for (StatementNode statement : program.getStatements()) {
            if (statement instanceof VarDeclarationNode) {
                VarDeclarationNode varDecl = (VarDeclarationNode) statement;
                symbolTable.addVariable(varDecl.getName());
            }
        }
        tempCellPosition = symbolTable.getNextMemoryCell();

        // Generate code for statements
        for (StatementNode statement : program.getStatements()) {
            generateStatement(statement);
        }
    }

    private void generateStatement(StatementNode statement) {
        if (statement instanceof VarDeclarationNode) {
            generateVarDeclaration((VarDeclarationNode) statement);
        } else if (statement instanceof PrintNode) {
            generatePrint((PrintNode) statement);
        } else if (statement instanceof WhileNode) {
            generateWhile((WhileNode) statement);
        } else if (statement instanceof AssignmentNode) {
            generateAssignment((AssignmentNode) statement);
        }
    }

    private void generateVarDeclaration(VarDeclarationNode varDecl) {
        // Variable already registered in symbol table during program init
        int position = symbolTable.getVariablePosition(varDecl.getName());

        // Move to the variable's position
        moveToPosition(position);

        // Generate code to evaluate the expression and store it in the variable
        generateExpression(varDecl.getValue());
    }

    private void generatePrint(PrintNode printNode) {
        // Evaluate the expression in the temp cell
        moveToPosition(tempCellPosition);
        code.append("[-]"); // Clear temp cell
        generateExpression(printNode.getExpression());

        // Print the value (ASCII)
        code.append(".");
    }

    private void generateWhile(WhileNode whileNode) {
        // Evaluate condition in temp cell
        moveToPosition(tempCellPosition);
        code.append("[-]"); // Clear temp cell
        generateExpression(whileNode.getCondition());

        // Start loop
        code.append("[");

        // Generate body statements
        for (StatementNode statement : whileNode.getBody()) {
            generateStatement(statement);
        }

        // Reevaluate condition
        moveToPosition(tempCellPosition);
        code.append("[-]"); // Clear temp cell
        generateExpression(whileNode.getCondition());

        // End loop
        code.append("]");
    }

    private void generateAssignment(AssignmentNode assignment) {
        int position = symbolTable.getVariablePosition(assignment.getName());

        // Clear the variable
        moveToPosition(position);
        code.append("[-]");

        // Generate code to evaluate the expression and store it in the variable
        generateExpression(assignment.getValue());
    }

    private void generateExpression(ExpressionNode expression) {
        if (expression instanceof NumberNode) {
            NumberNode numberNode = (NumberNode) expression;
            // Generate code to set current cell to the number value
            for (int i = 0; i < numberNode.getValue(); i++) {
                code.append("+");
            }
        } else if (expression instanceof VariableNode) {
            VariableNode varNode = (VariableNode) expression;
            int varPos = symbolTable.getVariablePosition(varNode.getName());

            // Copy variable value to current position
            int currentPos = getCurrentPosition();
            copyValue(varPos, currentPos);
        } else if (expression instanceof BinaryOpNode) {
            generateBinaryOp((BinaryOpNode) expression);
        }
    }

    private void generateBinaryOp(BinaryOpNode binaryOp) {
        // This is a simplified implementation - in real code you'd need more sophisticated handling

        // First, evaluate left side and store in temp cell
        moveToPosition(tempCellPosition);
        code.append("[-]"); // Clear temp cell
        generateExpression(binaryOp.getLeft());

        // For addition: evaluate right side and add it to the temp cell
        if (binaryOp.getOperator() == BinaryOpType.ADD) {
            // Save temp value
            int tempValue = tempCellPosition;
            int tempValue2 = tempCellPosition + 1;

            // Move to another temp position
            moveToPosition(tempValue2);
            code.append("[-]"); // Clear cell

            // Evaluate right side
            generateExpression(binaryOp.getRight());

            // Now add this value to the first temp cell
            code.append("["); // Start loop
            code.append("-"); // Decrement current cell
            moveToPosition(tempValue);
            code.append("+"); // Increment temp cell
            moveToPosition(tempValue2);
            code.append("]"); // End loop

            // Move back to original temp cell
            moveToPosition(tempValue);
        }
        // For subtraction: evaluate right side and subtract from temp cell
        else if (binaryOp.getOperator() == BinaryOpType.SUBTRACT) {
            // Similar to addition but with subtraction
            int tempValue = tempCellPosition;
            int tempValue2 = tempCellPosition + 1;

            // Move to another temp position
            moveToPosition(tempValue2);
            code.append("[-]"); // Clear cell

            // Evaluate right side
            generateExpression(binaryOp.getRight());

            // Now subtract this value from the first temp cell
            code.append("["); // Start loop
            code.append("-"); // Decrement current cell
            moveToPosition(tempValue);
            code.append("-"); // Decrement temp cell
            moveToPosition(tempValue2);
            code.append("]"); // End loop

            // Move back to original temp cell
            moveToPosition(tempValue);
        }
    }

    private int getCurrentPosition() {
        // This is a simplified placeholder - in a real implementation,
        // you would track the current position as you generate code
        return tempCellPosition;
    }

    private void moveToPosition(int position) {
        int current = getCurrentPosition();
        int diff = position - current;

        if (diff > 0) {
            for (int i = 0; i < diff; i++) {
                code.append(">");
            }
        } else if (diff < 0) {
            for (int i = 0; i < -diff; i++) {
                code.append("<");
            }
        }
    }

    private void copyValue(int fromPos, int toPos) {
        // This is a simplified implementation
        // In Brainfuck, copying values is complex and requires temporary cells
        int tempPos1 = tempCellPosition + 2;
        int tempPos2 = tempCellPosition + 3;

        // Clear destination and temp cells
        moveToPosition(toPos);
        code.append("[-]");
        moveToPosition(tempPos1);
        code.append("[-]");
        moveToPosition(tempPos2);
        code.append("[-]");

        // Go to source
        moveToPosition(fromPos);

        // Copy value to both destination and temp cell (destructively)
        code.append("["); // While source not 0
        code.append("-"); // Decrement source
        moveToPosition(toPos);
        code.append("+"); // Increment destination
        moveToPosition(tempPos1);
        code.append("+"); // Increment temp
        moveToPosition(fromPos);
        code.append("]"); // End while

        // Copy back from temp to source
        moveToPosition(tempPos1);
        code.append("["); // While temp not 0
        code.append("-"); // Decrement temp
        moveToPosition(fromPos);
        code.append("+"); // Increment source
        moveToPosition(tempPos1);
        code.append("]"); // End while

        // Return to destination
        moveToPosition(toPos);
    }
}