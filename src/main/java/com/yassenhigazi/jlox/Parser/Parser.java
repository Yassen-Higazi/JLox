package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.JLox;
import com.yassenhigazi.jlox.Scanner.Token;
import com.yassenhigazi.jlox.Scanner.TokenType;

import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTExpression parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private ASTExpression expression() {
        return equality();
    }

    private ASTExpression equality() {
        ASTExpression expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();

            ASTExpression right = comparison();

            expr = new ASTExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ASTExpression comparison() {
        ASTExpression expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();

            ASTExpression right = term();

            expr = new ASTExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ASTExpression term() {
        ASTExpression expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();

            ASTExpression right = factor();

            expr = new ASTExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ASTExpression factor() {
        ASTExpression expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();

            ASTExpression right = unary();

            expr = new ASTExpression.Binary(expr, operator, right);
        }

        return expr;
    }

    private ASTExpression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            ASTExpression right = unary();
            return new ASTExpression.Unary(operator, right);
        }

        return primary();
    }

    private ASTExpression primary() {
        if (match(TokenType.FALSE)) return new ASTExpression.Literal(false);

        if (match(TokenType.TRUE)) return new ASTExpression.Literal(true);

        if (match(TokenType.NIL)) return new ASTExpression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new ASTExpression.Literal(previous().literal);
        }

        if (match(TokenType.LEFT_PAREN)) {
            ASTExpression expr = expression();

            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");

            return new ASTExpression.Grouping(expr);
        }


        throw error(peek(), "Unexpect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        JLox.error(token, message);

        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;

        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private static class ParseError extends RuntimeException {
    }
}
