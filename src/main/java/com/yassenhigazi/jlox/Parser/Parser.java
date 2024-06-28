package com.yassenhigazi.jlox.Parser;

import com.yassenhigazi.jlox.Errors.EmptyAssignmentError;
import com.yassenhigazi.jlox.Errors.ParseError;
import com.yassenhigazi.jlox.JLox;
import com.yassenhigazi.jlox.Scanner.Token;
import com.yassenhigazi.jlox.Scanner.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;


    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<ASTStatement> parse() {
        List<ASTStatement> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private ASTStatement declaration() {
        try {
            if (match(TokenType.CLASS)) return classDeclaration();
            if (match(TokenType.FUN)) return function("function");
            if (match(TokenType.VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();

            return null;
        }
    }

    private ASTStatement classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");

        ASTExpression.Variable superclass = null;

        if (match(TokenType.LESS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.");

            superclass = new ASTExpression.Variable(previous());
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");

        List<ASTStatement.Function> methods = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");

        return new ASTStatement.Class(name, superclass, methods);
    }

    private ASTStatement.Function function(String kind) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");

        consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");

        List<Token> parameters = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {

            do {

                if (parameters.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 parameters.");
                }

                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));

            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");

        List<ASTStatement> body = block();

        return new ASTStatement.Function(name, parameters, body);
    }

    private ASTStatement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        ASTExpression initializer = null;

        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

        if (initializer == null)
            throw new EmptyAssignmentError("Variable can not be empty. try var " + name.lexeme + " = nil;");

        return new ASTStatement.Var(name, initializer);
    }

    private ASTStatement statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.RETURN)) return returnStatement();
        if (match(TokenType.WHILE)) return whileStatement();

        if (match(TokenType.LEFT_BRACE)) return new ASTStatement.Block(block());

        return expressionStatement();
    }

    private ASTStatement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");

        ASTExpression condition = expression();

        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        ASTStatement thenBranch = statement();

        ASTStatement elseBranch = null;

        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new ASTStatement.If(condition, thenBranch, elseBranch);
    }

    private ASTStatement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        ASTStatement initializer;

        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        ASTExpression condition = null;

        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        ASTExpression increment = null;

        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        ASTStatement body = statement();

        if (increment != null) {
            body = new ASTStatement.Block(Arrays.asList(body, new ASTStatement.Expression(increment)));
        }

        if (condition == null) condition = new ASTExpression.Literal(true);

        body = new ASTStatement.While(condition, body);

        if (initializer != null) {
            body = new ASTStatement.Block(Arrays.asList(initializer, body));
        }

        return body;
    }

    private ASTStatement returnStatement() {
        Token keyword = previous();

        ASTExpression value = null;

        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.");

        return new ASTStatement.Return(keyword, value);
    }

    private ASTStatement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");

        ASTExpression condition = expression();

        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

        ASTStatement body = statement();

        return new ASTStatement.While(condition, body);
    }

    private List<ASTStatement> block() {
        List<ASTStatement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");

        return statements;
    }

    private ASTStatement printStatement() {
        ASTExpression value = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after value.");

        return new ASTStatement.Print(value);
    }

    private ASTStatement expressionStatement() {
        ASTExpression expr = expression();

        consume(TokenType.SEMICOLON, "Expect ';' after expression.");

        return new ASTStatement.Expression(expr);
    }

    private ASTExpression expression() {
        return assignment();
    }

    private ASTExpression assignment() {
        ASTExpression expr = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();

            ASTExpression value = assignment();

            if (expr instanceof ASTExpression.Variable) {

                Token name = ((ASTExpression.Variable) expr).name;

                return new ASTExpression.Assign(name, value);
            } else if (expr instanceof ASTExpression.Get get) {

                return new ASTExpression.Set(get.object, get.name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private ASTExpression or() {
        ASTExpression expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();

            ASTExpression right = and();

            expr = new ASTExpression.Logical(expr, operator, right);
        }

        return expr;
    }

    private ASTExpression and() {
        ASTExpression expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();

            ASTExpression right = equality();

            expr = new ASTExpression.Logical(expr, operator, right);
        }

        return expr;
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

        return call();
    }

    private ASTExpression call() {
        ASTExpression expr = primary();

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");

                expr = new ASTExpression.Get(expr, name);
            } else {
                break;
            }
        }

        return expr;
    }

    private ASTExpression finishCall(ASTExpression callee) {
        List<ASTExpression> arguments = new ArrayList<>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    throw error(peek(), "Can't have more than 255 arguments.");
                }

                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

        return new ASTExpression.Call(callee, paren, arguments);
    }

    private ASTExpression primary() {
        if (match(TokenType.FALSE)) return new ASTExpression.Literal(false);

        if (match(TokenType.TRUE)) return new ASTExpression.Literal(true);

        if (match(TokenType.NIL)) return new ASTExpression.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new ASTExpression.Literal(previous().literal);
        }

        if (match(TokenType.SUPER)) {
            Token keyword = previous();

            consume(TokenType.DOT, "Expect '.' after 'super'.");

            Token method = consume(TokenType.IDENTIFIER, "Expect superclass method name.");
            
            return new ASTExpression.Super(keyword, method);
        }

        if (match(TokenType.THIS)) return new ASTExpression.This(previous());

        if (match(TokenType.IDENTIFIER)) {
            return new ASTExpression.Variable(previous());
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

}
