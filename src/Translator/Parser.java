package Translator;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.currentToken = lexer.getNextToken();
    }

    private void eat(TokenType tokenType) {
        if (currentToken.getType() == tokenType) {
            currentToken = lexer.getNextToken();
        } else {
            throw new RuntimeException("Syntax error: Expected " + tokenType + ", got " + currentToken.getType());
        }
    }

    public AST parse() {
        ProgramNode program = new ProgramNode();

        while (currentToken.getType() != TokenType.EOF) {
            StatementNode statement = parseStatement();
            program.addStatement(statement);
        }

        return program;
    }

    private StatementNode parseStatement() {
        switch (currentToken.getType()) {
            case VAR:
                return parseVarDeclaration();
            case PRINT:
                return parsePrintStatement();
            case WHILE:
                return parseWhileStatement();
            case IDENTIFIER:
                return parseAssignment();
            default:
                throw  new RuntimeException("Unexpected token: " + currentToken);
        }
    }

    private VarDeclarationNode parseVarDeclaration() {
        eat(TokenType.VAR);
        String name = currentToken.getValue();
        eat(TokenType.IDENTIFIER);
        eat(TokenType.ASSIGN);
        ExpressionNode value = parseExpression();
        return new VarDeclarationNode(name, value);
    }

    private PrintNode parsePrintStatement() {
        eat(TokenType.PRINT);
        ExpressionNode value = parseExpression();
        return new PrintNode(value);
    }

    private WhileNode parseWhileStatement() {
        eat(TokenType.WHILE);
        ExpressionNode condition = parseExpression();
        List<StatementNode> body = new ArrayList<>();

        while (currentToken.getType() != TokenType.END) {
            body.add(parseStatement());
        }

        eat(TokenType.END);
        return new WhileNode(condition, body);
    }

    private AssignmentNode parseAssignment() {
        String name = currentToken.getValue();
        eat(TokenType.IDENTIFIER);
        eat(TokenType.ASSIGN);
        ExpressionNode value = parseExpression();
        return new AssignmentNode(name, value);
    }

    private ExpressionNode parseExpression() {
        ExpressionNode left = parseTerm();

        while (currentToken.getType() == TokenType.PLUS ||
                currentToken.getType() == TokenType.MINUS) {
            TokenType operator = currentToken.getType();
            eat(operator);
            ExpressionNode right = parseTerm();

            if (operator == TokenType.PLUS) {
                left = new BinaryOpNode(left, right, BinaryOpType.ADD);
            } else {
                left = new BinaryOpNode(left, right, BinaryOpType.SUBTRACT);
            }
        }

        return left;
    }

    private ExpressionNode parseTerm() {
        if (currentToken.getType() == TokenType.NUMBER) {
            int value = Integer.parseInt(currentToken.getValue());
            eat(TokenType.NUMBER);
            return new NumberNode(value);
        } else if (currentToken.getType() == TokenType.IDENTIFIER) {
            String name = currentToken.getValue();
            eat(TokenType.IDENTIFIER);
            return new VariableNode(name);
        } else {
            throw new RuntimeException("Unexpected token: " + currentToken);
        }
    }
}