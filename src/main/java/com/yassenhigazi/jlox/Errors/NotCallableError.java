package com.yassenhigazi.jlox.Errors;

import com.yassenhigazi.jlox.Scanner.Token;

public class NotCallableError extends RuntimeError {

    public NotCallableError(Token token, String message) {
        super(token, "NotCallableError: " + message);
    }
}
