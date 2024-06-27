package com.yassenhigazi.jlox.Utils;


import com.yassenhigazi.jlox.Parser.ASTExpression;
import com.yassenhigazi.jlox.Parser.ASTStatement;
import com.yassenhigazi.jlox.Scanner.Token;

import java.util.List;

@SuppressWarnings("unused")
public class ASTPrinter implements ASTExpression.Visitor<String>, ASTStatement.Visitor<String> {

    public String printExpressions(List<ASTExpression> exprs) {
        StringBuilder result = new StringBuilder();

        for (ASTExpression expr : exprs) {
            result.append(this.print(expr));
        }

        return result.toString();
    }

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
    public String visitCallASTExpression(ASTExpression.Call expr) {
        return "function" + print(expr.callee) + "(" + printExpressions(expr.arguments) + ")";
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

    @Override
    public String visitLogicalASTExpression(ASTExpression.Logical expr) {
        return print(expr.left) + " " + expr.operator.lexeme + " " + print(expr.right);
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
    public String visitFunctionASTStatement(ASTStatement.Function expr) {
        StringBuilder builder = new StringBuilder();

        builder.append("fun ")
                .append(expr.name.lexeme)
                .append("<");

        for (Token param : expr.params) {
            builder.append(param.lexeme)
                    .append(", ");
        }

        builder.append(">");

        return builder.toString();
    }

    @Override
    public String visitPrintASTStatement(ASTStatement.Print expr) {
        return "print " + print(expr.expression);
    }

    @Override
    public String visitReturnASTStatement(ASTStatement.Return expr) {
        return "return " + print(expr.value);
    }

    @Override
    public String visitVarASTStatement(ASTStatement.Var expr) {
        StringBuilder builder = new StringBuilder();

        builder.append("var ").append(expr.name.lexeme);

        if (expr.initializer != null) builder.append(" = ").append(print(expr.initializer));

        return builder.toString();
    }

    @Override
    public String visitWhileASTStatement(ASTStatement.While expr) {

        return "While (" + print(expr.condition) + ")" + "{" + print(expr.body) + "}";
    }

    @Override
    public String visitIfASTStatement(ASTStatement.If expr) {
        StringBuilder builder = new StringBuilder();

        builder.append("if ").append(print(expr.condition));

        if (expr.elseBranch != null) builder.append(" else ").append(print(expr.elseBranch));

        return builder.toString();
    }
}
