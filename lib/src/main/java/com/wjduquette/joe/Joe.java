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
    // Script Execution

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

    //-------------------------------------------------------------------------
    // Output and Error Handling

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
    // Services provided to the rest of the implementation

    /**
     * Stringify converts an object to its string representation,
     * <b>as visible at the scripting layer</b>. Normally, this is the same
     * as {@code object.toString()}, but there are special cases.
     *
     * <ul>
     * <li>The null value stringifies as "null".</li>
     * <li>Doubles with integer values stringify without the ".0".</li>
     * <li>Instances of Joe classes can provide a stringifier.</li>
     * <li>Java class bindings can provide a stringifier.</li>
     * </ul>
     *
     * <p>This method would be static except that it will need to reference
     * the engine's data for non-standard types.</p>
     * @param value The value being stringified.
     * @return The string
     */
    public String stringify(Object value) {
        if (value == null) return "null";

        if (value instanceof Double) {
            String text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    /**
     * Returns true if the object is "truthy", i.e., boolean {@code true} or
     * non-null, and false otherwise.
     * @param value The value
     * @return true or false
     */
    public static boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean)value;
        return true;
    }

    /**
     * Returns true if the two values are equal, and false otherwise.
     * This is essentially equivalent to {@code Objects.equals(a, b)}.
     * @param a The first value
     * @param b The second value
     * @return true or false
     */
    public static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    //-------------------------------------------------------------------------
    // Main
    //
    // This will be extracted as JoeApp

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
