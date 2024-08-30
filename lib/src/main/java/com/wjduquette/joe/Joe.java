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

    // The actual interpreter.  Retained because it owns the global environment.
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

    public void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            var result = run(line);
            if (!hadError && result != null) {
                System.out.println("-> " + stringify(result));
            }
            hadError = false;
        }
    }

    public Object run(String source) {
        Scanner scanner = new Scanner(this, source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(this, tokens);
        var statements = parser.parse();
        System.out.println("<<<\n" + recodify(statements) + "\n>>>");

        // Stop if there was a syntax error.
        if (hadError) return null;

        try {
            return interpreter.interpret(statements);
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

    // Converts the expression into something that looks like code.
    String recodify(Expr expr) {
        return Codifier.codify(this, expr);
    }

    // Converts the statement into something that looks like code.
    String recodify(Stmt statement) {
        return recodify(List.of(statement));
    }

    // Converts the statements into something that looks like code.
    String recodify(List<Stmt> statements) {
        return Codifier.codify(this, statements);
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
     * Converts a value to a string as it would appear in Monica code.
     * This is intended primarily for use in error messages, but
     * could also be used during code generation.
     * @param value The value
     * @return The value
     */
    public String codify(Object value) {
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        } else {
            return stringify(value);
        }
    }

    /**
     * Given an arbitrary string, escapes all typical control characters as
     * they would appear in Java or Joe code.
     * TODO: Handle unicode escapes
     * @param string The input string
     * @return The string with escapes
     */
    public static String escape(String string){
        var buff = new StringBuilder();

        for (int i = 0; i < string.length(); i++) {
            var c = string.charAt(i);
            switch (c) {
                case '\\' -> buff.append("\\\\");
                case '\t' -> buff.append("\\t");
                case '\b' -> buff.append("\\b");
                case '\n' -> buff.append("\\n");
                case '\r' -> buff.append("\\r");
                case '\f' -> buff.append("\\f");
                case '"' -> buff.append("\\\"");
                default -> {
                    if (c < 256) {
                        buff.append(c);
                    } else {
                        var hex = (int)c;
                        buff.append(String.format("\\u%04X", hex));
                    }
                }
            }
        }
        return buff.toString();
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
