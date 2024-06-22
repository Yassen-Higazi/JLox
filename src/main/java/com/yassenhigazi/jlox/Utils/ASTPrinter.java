package com.yassenhigazi.jlox.Utils;


import com.yassenhigazi.jlox.Parser.ASTExpression;
import com.yassenhigazi.jlox.Parser.ASTStatement;

import java.util.List;

@SuppressWarnings("unused")
public class ASTPrinter implements ASTExpression.Visitor<String>, ASTStatement.Visitor<String> {

    public String print(ASTExpression expr) {
        return expr.accept(this);
    }

    public String print(List<ASTStatement> statement) {
        StringBuilder builder = new StringBuilder();

        for (ASTStatement stmt : statement) {
            String str = print(stmt);

            builder.append("\n").append(str);
        }

        return builder.toString();
    }

    public String print(ASTStatement statement) {
        return statement.accept(this);
    }

    @Override
    public String visitBinaryASTExpression(ASTExpression.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingASTExpression(ASTExpression.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralASTExpression(ASTExpression.Literal expr) {
        if (expr.value == null) return "nil";

        return expr.value.toString();
    }

    @Override
    public String visitUnaryASTExpression(ASTExpression.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableASTExpression(ASTExpression.Variable expr) {
        return "var " + expr.name.lexeme;
    }

    @Override
    public String visitAssignASTExpression(ASTExpression.Assign expr) {
        return "Assignment " + expr.name.lexeme + " = " + print(expr.value);
    }

    private String parenthesize(String name, ASTExpression... expressions) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);

        for (ASTExpression expr : expressions) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitBlockASTStatement(ASTStatement.Block expr) {
        return print(expr.statements);
    }

    @Override
    public String visitExpressionASTStatement(ASTStatement.Expression expr) {
        return print(expr.expression);
    }

    @Override
    public String visitPrintASTStatement(ASTStatement.Print expr) {
        return "print " + print(expr.expression);
    }

    @Override
    public String visitVarASTStatement(ASTStatement.Var expr) {
        StringBuilder builder = new StringBuilder();

        builder.append("var ").append(expr.name.lexeme);

        if (expr.initializer != null) builder.append(" = ").append(print(expr.initializer));

        return builder.toString();
    }
}
