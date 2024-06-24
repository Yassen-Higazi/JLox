package com.yassenhigazi.jlox;

import com.yassenhigazi.jlox.Errors.RuntimeError;
import com.yassenhigazi.jlox.Parser.ASTStatement;
import com.yassenhigazi.jlox.Parser.Interpreter;
import com.yassenhigazi.jlox.Parser.Parser;
import com.yassenhigazi.jlox.Scanner.JLoxScanner;
import com.yassenhigazi.jlox.Scanner.Token;
import com.yassenhigazi.jlox.Scanner.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JLox {
    private static final Interpreter interpreter = new Interpreter();

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));

        try {
            run(new String(bytes, Charset.defaultCharset()));

        } catch (RuntimeError e) {
            error(e);
        }

        // exist if there is an error
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        for (; ; ) {
            System.out.print("jlox> ");

            String line = readPrompt();

            if (line == null) break;

            try {
                run(line);
            } catch (RuntimeError e) {
                error(e);
            }

            // reset error flag so prompt doesn't end
            hadError = false;
        }
    }

    private static String readPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        StringBuilder source = new StringBuilder();

        String line = reader.readLine();

        source.append(line);

        if (line == null) return null;

        boolean isBlock = line.endsWith("{");
        boolean isBlockClosed = false;

        while (isBlock && !isBlockClosed) {
            line = reader.readLine();

            source.append(line);

            isBlockClosed = line.endsWith("}");
        }

        return source.toString();
    }

    private static void run(String source) {
        JLoxScanner scanner = new JLoxScanner(source);

        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);

        List<ASTStatement> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        interpreter.interpret(statements);
    }

    public static void error(RuntimeError error) {
        report(0, 0, null, error.getMessage());
    }

    public static void error(int line, int column, String message) {
        report(line, column, null, message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println("\n[line " + error.token.line + "] " + error.getMessage());

        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        report(line, line, where, message);
    }

    private static void report(int line, int column, String where, String message) {
        String m = "";

        if (line != 0) {
            m = ("[line " + line);
        }

        if (column != 0) {
            m = " at column " + column;
        }

        if (line != 0) {
            m += "]";
        }

        if (where != null) {
            m += "Error at: " + where;
        }

        m += message;

        System.err.println(m);

        hadError = true;
    }
}