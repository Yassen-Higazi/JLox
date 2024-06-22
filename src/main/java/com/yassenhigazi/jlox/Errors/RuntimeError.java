package com.yassenhigazi.jlox.Errors;

import com.yassenhigazi.jlox.Scanner.Token;

public class RuntimeError extends RuntimeException {
    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
