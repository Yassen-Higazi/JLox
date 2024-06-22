package com.yassenhigazi.jlox.Errors;

import com.yassenhigazi.jlox.Scanner.Token;

public class UndefinedVariableError extends RuntimeError {

    public UndefinedVariableError(Token token, String message) {
        super(token, "UndefinedVariableError: " + message);
    }
}
