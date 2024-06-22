package com.yassenhigazi.jlox;

import com.yassenhigazi.jlox.Parser.ASTExpression;
import com.yassenhigazi.jlox.Parser.Parser;
import com.yassenhigazi.jlox.Scanner.JLoxScanner;
import com.yassenhigazi.jlox.Scanner.Token;
import com.yassenhigazi.jlox.Scanner.TokenType;
import com.yassenhigazi.jlox.Utils.ASTPrinter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JLox {
    static boolean hadError = false;

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

        run(new String(bytes, Charset.defaultCharset()));

        // exist if there is an error
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("jlox> ");

            String line = reader.readLine();

            if (line == null) break;

            run(line);

//            reset error flag so prompt doesn't end
            hadError = false;
        }
    }

    private static void run(String source) {
        JLoxScanner scanner = new JLoxScanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
//        for (Token token : tokens) {
//            System.out.println(token);
//        }

        Parser parser = new Parser(tokens);
        ASTExpression expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        System.out.println(new ASTPrinter().print(expression));
    }

    public static void error(int line, String message) {
        error(line, 0, message);
    }

    public static void error(int line, int column, String message) {
        report(line, column, "", message);
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        report(line, line, where, message);
    }

    private static void report(int line, int column, String where, String message) {
        String lineAndColumn = "[line " + line + (column != 0 ? column + "]" : "]");

        String errMessage = "Error" + where + ": " + message;

        System.err.println(lineAndColumn + " " + errMessage);

        hadError = true;
    }
}