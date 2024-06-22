package com.yassenhigazi.jlox.Utils;


import com.yassenhigazi.jlox.Parser.ASTExpression;

public class ASTPrinter implements ASTExpression.Visitor<String> {

    public String print(ASTExpression expr) {
        return expr.accept(this);
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
}
