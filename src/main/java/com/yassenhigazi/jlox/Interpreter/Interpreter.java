package com.yassenhigazi.jlox.Interpreter;

import com.yassenhigazi.jlox.Environment.Environment;
import com.yassenhigazi.jlox.Errors.NotCallableError;
import com.yassenhigazi.jlox.Errors.Return;
import com.yassenhigazi.jlox.Errors.RuntimeError;
import com.yassenhigazi.jlox.Errors.ZeroDivisionError;
import com.yassenhigazi.jlox.JLox;
import com.yassenhigazi.jlox.Parser.ASTExpression;
import com.yassenhigazi.jlox.Parser.ASTStatement;
import com.yassenhigazi.jlox.Scanner.Token;
import com.yassenhigazi.jlox.Scanner.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements ASTExpression.Visitor<Object>, ASTStatement.Visitor<Void> {

    final Environment globals = new Environment();
    private final Map<ASTExpression, Integer> locals = new HashMap<>();
    private Environment environment = globals;

    public void interpret(List<ASTStatement> statements) {
        globals.define("clock", new ClockMethod());

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
    public Object visitCallASTExpression(ASTExpression.Call expr) {
        Object callee = evaluate(expr.callee);

        if (!(callee instanceof LoxCallable)) {
            throw new NotCallableError(expr.paren, "Can only call functions and classes.");
        }

        List<Object> arguments = new ArrayList<>();

        for (ASTExpression argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        //noinspection PatternVariableCanBeUsed
        LoxCallable function = (LoxCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
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
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitAssignASTExpression(ASTExpression.Assign expr) {
        Object value = evaluate(expr.value);
        
        Integer distance = locals.get(expr);

        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }


        environment.assign(expr.name, value);

        return value;
    }

    @Override
    public Object visitLogicalASTExpression(ASTExpression.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
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
    public Void visitFunctionASTStatement(ASTStatement.Function statement) {
        LoxFunction function = new LoxFunction(statement, environment);

        environment.define(statement.name.lexeme, function);

        return null;
    }

    @Override
    public Void visitPrintASTStatement(ASTStatement.Print statement) {
        Object value = evaluate(statement.expression);

        System.out.println(stringify(value));

        return null;
    }

    @Override
    public Void visitReturnASTStatement(ASTStatement.Return statement) {
        Object value = null;

        if (statement.value != null) value = evaluate(statement.value);

        throw new Return(value);
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

    @Override
    public Void visitWhileASTStatement(ASTStatement.While expr) {

        while (isTruthy(evaluate(expr.condition))) {
            execute(expr.body);
        }

        return null;
    }

    @Override
    public Void visitIfASTStatement(ASTStatement.If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }

        return null;
    }

    private Object evaluate(ASTExpression expr) {
        return expr.accept(this);
    }

    private void execute(ASTStatement stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<ASTStatement> statements, Environment environment) {
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

    public void resolve(ASTExpression expr, int depth) {
        locals.put(expr, depth);
    }

    private Object lookUpVariable(Token name, ASTExpression expr) {
        Integer distance = locals.get(expr);

        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
    }
}
