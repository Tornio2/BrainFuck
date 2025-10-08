package Translator;

import java.util.ArrayList;
import java.util.List;

// Base AST node
public abstract class AST {
}

class ProgramNode extends AST {
    private final List<StatementNode> statements = new ArrayList<>();

    public void addStatement(StatementNode statement) {
        statements.add(statement);
    }

    public List<StatementNode> getStatements() {
        return statements;
    }
}

// Statement nodes
abstract class StatementNode extends AST {
}

class VarDeclarationNode extends StatementNode {
    private final String name;
    private final ExpressionNode value;

    public VarDeclarationNode(String name, ExpressionNode value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ExpressionNode getValue() {
        return value;
    }
}

class PrintNode extends StatementNode {
    private final ExpressionNode expression;

    public PrintNode(ExpressionNode expression) {
        this.expression = expression;
    }

    public ExpressionNode getExpression() {
        return expression;
    }
}

class WhileNode extends StatementNode {
    private final ExpressionNode condition;
    private final List<StatementNode> body;

    public WhileNode(ExpressionNode condition, List<StatementNode> body) {
        this.condition = condition;
        this.body = body;
    }

    public ExpressionNode getCondition() {
        return condition;
    }

    public List<StatementNode> getBody() {
        return body;
    }
}

class AssignmentNode extends StatementNode {
    private final String name;
    private final ExpressionNode value;

    public AssignmentNode(String name, ExpressionNode value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public ExpressionNode getValue() {
        return value;
    }
}

// Expression nodes
abstract class ExpressionNode extends AST {
}

class NumberNode extends ExpressionNode {
    private final int value;

    public NumberNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

class VariableNode extends ExpressionNode {
    private final String name;

    public VariableNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

enum BinaryOpType {
    ADD, SUBTRACT, MULTIPLY, DIVIDE, GREATER_THAN, LESS_THAN, EQUALS
}

class BinaryOpNode extends ExpressionNode {
    private final ExpressionNode left;
    private final ExpressionNode right;
    private final BinaryOpType operator;

    public BinaryOpNode(ExpressionNode left, ExpressionNode right, BinaryOpType operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public ExpressionNode getLeft() {
        return left;
    }

    public ExpressionNode getRight() {
        return right;
    }

    public BinaryOpType getOperator() {
        return operator;
    }
}