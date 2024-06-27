package com.yassenhigazi.jlox.Resolver;

import com.yassenhigazi.jlox.Interpreter.Interpreter;
import com.yassenhigazi.jlox.JLox;
import com.yassenhigazi.jlox.Parser.ASTExpression;
import com.yassenhigazi.jlox.Parser.ASTStatement;
import com.yassenhigazi.jlox.Scanner.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements ASTExpression.Visitor<Void>, ASTStatement.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<ASTStatement> statements) {
        for (ASTStatement statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(ASTStatement stmt) {
        stmt.accept(this);
    }

    private void resolve(ASTExpression expr) {
        expr.accept(this);
    }

    @Override
    public Void visitBinaryASTExpression(ASTExpression.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallASTExpression(ASTExpression.Call expr) {
        resolve(expr.callee);

        for (ASTExpression argument : expr.arguments) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingASTExpression(ASTExpression.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralASTExpression(ASTExpression.Literal expr) {
        return null;
    }

    @Override
    public Void visitUnaryASTExpression(ASTExpression.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableASTExpression(ASTExpression.Variable expr) {

        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE) {
            JLox.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitAssignASTExpression(ASTExpression.Assign expr) {
        resolve(expr.value);

        resolveLocal(expr, expr.name);

        return null;
    }

    @Override
    public Void visitLogicalASTExpression(ASTExpression.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitBlockASTStatement(ASTStatement.Block statement) {
        beginScope();

        resolve(statement.statements);

        endScope();

        return null;
    }

    @Override
    public Void visitExpressionASTStatement(ASTStatement.Expression statement) {
        resolve(statement.expression);

        return null;
    }

    @Override
    public Void visitFunctionASTStatement(ASTStatement.Function statement) {
        declare(statement.name);

        define(statement.name);

        resolveFunction(statement, FunctionType.FUNCTION);

        return null;
    }

    @Override
    public Void visitPrintASTStatement(ASTStatement.Print statement) {
        resolve(statement.expression);

        return null;
    }

    @Override
    public Void visitReturnASTStatement(ASTStatement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            JLox.error(statement.keyword, "Can't return from top-level code.");
        }

        if (statement.value != null) {
            resolve(statement.value);
        }

        return null;
    }

    @Override
    public Void visitVarASTStatement(ASTStatement.Var statement) {
        declare(statement.name);

        if (statement.initializer != null) {
            resolve(statement.initializer);
        }

        define(statement.name);

        return null;
    }

    @Override
    public Void visitWhileASTStatement(ASTStatement.While statement) {
        resolve(statement.condition);

        resolve(statement.body);

        return null;
    }

    @Override
    public Void visitIfASTStatement(ASTStatement.If statement) {
        resolve(statement.condition);

        resolve(statement.thenBranch);

        if (statement.elseBranch != null) resolve(statement.elseBranch);

        return null;
    }

    private void resolveLocal(ASTExpression expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);

                return;
            }
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void resolveFunction(ASTStatement.Function function, FunctionType type) {

        FunctionType enclosingFunction = currentFunction;

        currentFunction = type;

        beginScope();

        for (Token param : function.params) {
            declare(param);

            define(param);
        }

        resolve(function.body);

        endScope();

        currentFunction = enclosingFunction;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();

        if (scope.containsKey(name.lexeme)) {
            JLox.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;

        scopes.peek().put(name.lexeme, true);
    }
}
