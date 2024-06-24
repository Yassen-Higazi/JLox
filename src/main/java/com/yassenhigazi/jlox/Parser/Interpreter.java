package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Environment.Environment;
import com.yassenhigazi.jlox.Errors.RuntimeError;
import com.yassenhigazi.jlox.Errors.ZeroDivisionError;
import com.yassenhigazi.jlox.JLox;
import com.yassenhigazi.jlox.Scanner.Token;

import java.util.List;

public class Interpreter implements ASTExpression.Visitor<Object>, ASTStatement.Visitor<Void> {

    private Environment environment = new Environment();

    public void interpret(List<ASTStatement> statements) {
        try {
            for (ASTStatement statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            JLox.runtimeError(error);
        }
    }

    @Override
    public Object visitBinaryASTExpression(ASTExpression.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left > (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) > 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case GREATER_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left >= (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) >= 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case LESS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left < (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) < 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case LESS_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left <= (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) <= 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case EQUAL_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left == (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) == 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case BANG_EQUAL:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left != (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return ((String) left).compareTo((String) right) != 0;
                }

                throw new RuntimeError(expr.operator, "Operands must be numbers or two strings.");

            case MINUS:
                if (!validateNumberOperands(expr.operator, left, right)) break;

                return (double) left - (double) right;

            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return left + (String) right;
                }

                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            case SLASH:
                if (left instanceof Double && right instanceof Double) {
                    if ((double) left == 0.0 || (double) right == 0.0)
                        throw new ZeroDivisionError(expr.operator, "Can not Divide by zero.");

                    return (double) left / (double) right;
                }

                throw new RuntimeError(expr.operator, "Operands must be two numbers.");

            case STAR:
                if (!validateNumberOperands(expr.operator, left, right)) break;

                return (double) left * (double) right;
        }

        return null;
    }

    @Override
    public Object visitGroupingASTExpression(ASTExpression.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralASTExpression(ASTExpression.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryASTExpression(ASTExpression.Unary expr) {
        Object right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case BANG -> !isTruthy(right);

            case MINUS -> -(double) right;

            default -> null;
        };

    }

    @Override
    public Object visitVariableASTExpression(ASTExpression.Variable expr) {
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignASTExpression(ASTExpression.Assign expr) {
        Object value = evaluate(expr.value);

        environment.assign(expr.name, value);

        return value;
    }

    @Override
    public Void visitBlockASTStatement(ASTStatement.Block expr) {
        executeBlock(expr.statements, new Environment(environment));

        return null;
    }

    @Override
    public Void visitExpressionASTStatement(ASTStatement.Expression statement) {
        evaluate(statement.expression);

        return null;
    }

    @Override
    public Void visitPrintASTStatement(ASTStatement.Print statement) {
        Object value = evaluate(statement.expression);

        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void visitVarASTStatement(ASTStatement.Var statement) {
        Object value = null;

        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }

        environment.define(statement.name.lexeme, value);

        return null;
    }

    private Object evaluate(ASTExpression expr) {
        return expr.accept(this);
    }

    private void execute(ASTStatement stmt) {
        stmt.accept(this);
    }

    private void executeBlock(List<ASTStatement> statements, Environment environment) {
        Environment previous = this.environment;

        try {
            this.environment = environment;

            for (ASTStatement statement : statements) {
                execute(statement);
            }

        } finally {
            this.environment = previous;
        }
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;

        if (object instanceof Boolean) return (boolean) object;

        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean validateNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return true;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();

            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }

            return text;
        }

        return object.toString();
    }
}
