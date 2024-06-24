package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Scanner.Token;

public abstract class ASTExpression {
    
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBinaryASTExpression(Binary expr);

        R visitGroupingASTExpression(Grouping expr);

        R visitLiteralASTExpression(Literal expr);

        R visitUnaryASTExpression(Unary expr);

        R visitVariableASTExpression(Variable expr);

        R visitAssignASTExpression(Assign expr);

        R visitLogicalASTExpression(Logical expr);
    }

    public static class Binary extends ASTExpression {
        public final ASTExpression left;
        public final Token operator;
        public final ASTExpression right;
        public Binary(ASTExpression left, Token operator, ASTExpression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryASTExpression(this);
        }
    }

    public static class Grouping extends ASTExpression {
        public final ASTExpression expression;

        public Grouping(ASTExpression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingASTExpression(this);
        }
    }

    public static class Literal extends ASTExpression {
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralASTExpression(this);
        }
    }

    public static class Unary extends ASTExpression {
        public final Token operator;
        public final ASTExpression right;

        public Unary(Token operator, ASTExpression right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryASTExpression(this);
        }
    }

    public static class Variable extends ASTExpression {
        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableASTExpression(this);
        }
    }

    public static class Assign extends ASTExpression {
        public final Token name;
        public final ASTExpression value;

        public Assign(Token name, ASTExpression value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignASTExpression(this);
        }
    }

    public static class Logical extends ASTExpression {
        public final ASTExpression left;
        public final Token operator;
        public final ASTExpression right;
        public Logical(ASTExpression left, Token operator, ASTExpression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalASTExpression(this);
        }
    }
}
