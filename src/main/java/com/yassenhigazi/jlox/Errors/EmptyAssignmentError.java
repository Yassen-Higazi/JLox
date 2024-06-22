package com.yassenhigazi.jlox.Errors;

public class EmptyAssignmentError extends RuntimeError {

    public EmptyAssignmentError(String message) {
        super(null, "EmptyAssignmentError: " + message);
    }
}
