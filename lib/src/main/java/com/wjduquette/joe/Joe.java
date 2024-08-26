package com.wjduquette.joe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Joe {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The actual interpreter.  Not sure why we need to retain it.
    private final Interpreter interpreter;
    boolean hadError = false;
    boolean hadRuntimeError = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Joe() {
        interpreter = new Interpreter(this);
    }

    //-------------------------------------------------------------------------
    // Public API

    @SuppressWarnings("UnusedReturnValue")
    public Object runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        var result = run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);

        return result;
    }

    private void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private Object run(String source) {
        Scanner scanner = new Scanner(this, source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(this, tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return null;

        try {
            return interpreter.interpret(expression);
        } catch (RuntimeError ex) {
            runtimeError(ex);
            return null;
        }
    }

    private void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.line() + "]");
        hadRuntimeError = true;
    }

    void error(int line, String message) {
        report(line, "", message);
    }

    private void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    //-------------------------------------------------------------------------
    // Main: This will be extracted as JoeApp

    public static void main(String[] args) throws IOException {
        var joe = new Joe();

        if (args.length > 1) {
            System.out.println("Usage: joe [script]");
            System.exit(64);
        } else if (args.length == 1) {
            joe.runFile(args[0]);
        } else {
            joe.runPrompt();
        }
    }
}
