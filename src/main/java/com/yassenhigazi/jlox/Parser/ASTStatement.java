package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Scanner.Token;

import java.util.List;

public abstract class ASTStatement {
  
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBlockASTStatement(Block expr);

        R visitExpressionASTStatement(Expression expr);

        R visitPrintASTStatement(Print expr);

        R visitVarASTStatement(Var expr);

        R visitWhileASTStatement(While expr);

        R visitIfASTStatement(If expr);
    }

    public static class Block extends ASTStatement {
        public final List<ASTStatement> statements;

        public Block(List<ASTStatement> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockASTStatement(this);
        }
    }

    public static class Expression extends ASTStatement {
        public final ASTExpression expression;

        public Expression(ASTExpression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionASTStatement(this);
        }
    }

    public static class Print extends ASTStatement {
        public final ASTExpression expression;

        public Print(ASTExpression expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintASTStatement(this);
        }
    }

    public static class Var extends ASTStatement {
        public final Token name;
        public final ASTExpression initializer;

        public Var(Token name, ASTExpression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarASTStatement(this);
        }
    }

    public static class While extends ASTStatement {
        public final ASTExpression condition;
        public final ASTStatement body;

        public While(ASTExpression condition, ASTStatement body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileASTStatement(this);
        }
    }

    public static class If extends ASTStatement {
        public final ASTExpression condition;
        public final ASTStatement thenBranch;
        public final ASTStatement elseBranch;
        public If(ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfASTStatement(this);
        }
    }
}
