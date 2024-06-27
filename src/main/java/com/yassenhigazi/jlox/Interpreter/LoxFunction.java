package com.yassenhigazi.jlox.Interpreter;

import com.yassenhigazi.jlox.Environment.Environment;
import com.yassenhigazi.jlox.Errors.Return;
import com.yassenhigazi.jlox.Parser.ASTStatement;

import java.util.List;

class LoxFunction implements LoxCallable {
    private final ASTStatement.Function declaration;

    private final Environment closure;

    LoxFunction(ASTStatement.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return this.declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {

        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}