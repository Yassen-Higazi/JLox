package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Scanner.Token;
import java.util.List;

public abstract class ASTExpression {
  public interface Visitor<R> {
    R visitBinaryASTExpression(Binary expr);
    R visitCallASTExpression(Call expr);
    R visitGetASTExpression(Get expr);
    R visitGroupingASTExpression(Grouping expr);
    R visitLiteralASTExpression(Literal expr);
    R visitSetASTExpression(Set expr);
    R visitThisASTExpression(This expr);
    R visitUnaryASTExpression(Unary expr);
    R visitVariableASTExpression(Variable expr);
    R visitAssignASTExpression(Assign expr);
    R visitLogicalASTExpression(Logical expr);
  }
  public static class Binary extends ASTExpression {
    public Binary(ASTExpression left, Token operator, ASTExpression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryASTExpression(this);
    }

    public final ASTExpression left;
    public final Token operator;
    public final ASTExpression right;
  }
  public static class Call extends ASTExpression {
    public Call(ASTExpression callee, Token paren, List<ASTExpression> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallASTExpression(this);
    }

    public final ASTExpression callee;
    public final Token paren;
    public final List<ASTExpression> arguments;
  }
  public static class Get extends ASTExpression {
    public Get(ASTExpression object, Token name) {
      this.object = object;
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGetASTExpression(this);
    }

    public final ASTExpression object;
    public final Token name;
  }
  public static class Grouping extends ASTExpression {
    public Grouping(ASTExpression expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingASTExpression(this);
    }

    public final ASTExpression expression;
  }
  public static class Literal extends ASTExpression {
    public Literal(Object value) {
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralASTExpression(this);
    }

    public final Object value;
  }
  public static class Set extends ASTExpression {
    public Set(ASTExpression object, Token name, ASTExpression value) {
      this.object = object;
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitSetASTExpression(this);
    }

    public final ASTExpression object;
    public final Token name;
    public final ASTExpression value;
  }
  public static class This extends ASTExpression {
    public This(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitThisASTExpression(this);
    }

    public final Token keyword;
  }
  public static class Unary extends ASTExpression {
    public Unary(Token operator, ASTExpression right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryASTExpression(this);
    }

    public final Token operator;
    public final ASTExpression right;
  }
  public static class Variable extends ASTExpression {
    public Variable(Token name) {
      this.name = name;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableASTExpression(this);
    }

    public final Token name;
  }
  public static class Assign extends ASTExpression {
    public Assign(Token name, ASTExpression value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignASTExpression(this);
    }

    public final Token name;
    public final ASTExpression value;
  }
  public static class Logical extends ASTExpression {
    public Logical(ASTExpression left, Token operator, ASTExpression right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalASTExpression(this);
    }

    public final ASTExpression left;
    public final Token operator;
    public final ASTExpression right;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}
