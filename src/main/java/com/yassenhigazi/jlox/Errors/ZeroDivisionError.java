package com.yassenhigazi.jlox.Errors;

import com.yassenhigazi.jlox.Scanner.Token;

public class ZeroDivisionError extends RuntimeError {

    public ZeroDivisionError(Token token, String message) {
        super(token, "ZeroDivisionError: " + message);
    }
}
