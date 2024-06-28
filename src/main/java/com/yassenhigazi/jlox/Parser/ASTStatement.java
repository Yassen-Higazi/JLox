package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Scanner.Token;
import java.util.List;

public abstract class ASTStatement {
  public interface Visitor<R> {
    R visitBlockASTStatement(Block expr);
    R visitClassASTStatement(Class expr);
    R visitExpressionASTStatement(Expression expr);
    R visitFunctionASTStatement(Function expr);
    R visitPrintASTStatement(Print expr);
    R visitReturnASTStatement(Return expr);
    R visitVarASTStatement(Var expr);
    R visitWhileASTStatement(While expr);
    R visitIfASTStatement(If expr);
  }
  public static class Block extends ASTStatement {
    public Block(List<ASTStatement> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockASTStatement(this);
    }

    public final List<ASTStatement> statements;
  }
  public static class Class extends ASTStatement {
    public Class(Token name, List<ASTStatement.Function> methods) {
      this.name = name;
      this.methods = methods;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassASTStatement(this);
    }

    public final Token name;
    public final List<ASTStatement.Function> methods;
  }
  public static class Expression extends ASTStatement {
    public Expression(ASTExpression expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionASTStatement(this);
    }

    public final ASTExpression expression;
  }
  public static class Function extends ASTStatement {
    public Function(Token name, List<Token> params, List<ASTStatement> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionASTStatement(this);
    }

    public final Token name;
    public final List<Token> params;
    public final List<ASTStatement> body;
  }
  public static class Print extends ASTStatement {
    public Print(ASTExpression expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintASTStatement(this);
    }

    public final ASTExpression expression;
  }
  public static class Return extends ASTStatement {
    public Return(Token keyword, ASTExpression value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnASTStatement(this);
    }

    public final Token keyword;
    public final ASTExpression value;
  }
  public static class Var extends ASTStatement {
    public Var(Token name, ASTExpression initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarASTStatement(this);
    }

    public final Token name;
    public final ASTExpression initializer;
  }
  public static class While extends ASTStatement {
    public While(ASTExpression condition, ASTStatement body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileASTStatement(this);
    }

    public final ASTExpression condition;
    public final ASTStatement body;
  }
  public static class If extends ASTStatement {
    public If(ASTExpression condition, ASTStatement thenBranch, ASTStatement elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfASTStatement(this);
    }

    public final ASTExpression condition;
    public final ASTStatement thenBranch;
    public final ASTStatement elseBranch;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}
