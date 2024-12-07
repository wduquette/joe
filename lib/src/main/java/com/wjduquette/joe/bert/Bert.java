package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SyntaxError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

// Temporary main program.  Later this will be `BertEngine`, and we will
// invoke it from the main app.
public class Bert {
    public static void main(String[] args) {
        System.out.println("Bert!");

        if (args.length == 0) {
            repl();
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.out.println("Usage: bert [path]");
            System.exit(64);
        }
    }

    //-------------------------------------------------------------------------
    // Execution

    public static void repl() {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        var vm = new VirtualMachine();

        for (;;) {
            System.out.print("> ");
            try {
                String line = reader.readLine();
                if (line == null) break;
                vm.interpret(line);
            } catch (IOException ex) {
                System.out.println("*** " + ex.getMessage());
            } catch (SyntaxError ex) {
                System.out.println(ex.getErrorReport());
                System.out.println("*** " + ex.getMessage());
            } catch (JoeError ex) {
                System.out.println("*** " + ex.getJoeStackTrace());
            }
        }
    }

    public static void runFile(String filename) {
        var vm = new VirtualMachine();

        try {
            var script = Files.readString(Path.of(filename));
            vm.interpret(script);
        } catch (IOException ex) {
            System.out.println("*** " + ex.getMessage());
        } catch (SyntaxError ex) {
            System.out.println(ex.getErrorReport());
            System.out.println("*** " + ex.getMessage());
        } catch (JoeError ex) {
            System.out.println("*** " + ex.getJoeStackTrace());
        }
    }

    //-------------------------------------------------------------------------
    // Stand-ins

    // Stand in for Joe.isFalsey (which doesn't exist yet)
    public static boolean isFalsey(Object value) {
        return !Joe.isTruthy(value);
    }

    // Stand in for Joe.isEqual
    public static boolean isEqual(Object a, Object b) {
        return Joe.isEqual(a, b);
    }

    // Stand in for BertEngine::isDebug
    public static boolean isDebug() {
        return true;
    }

    // Stand in for Joe::stringify
    public static String stringify(Object value) {
        return switch (value) {
            case null -> "null";
            case Double d -> {
                var s = d.toString();
                yield s.endsWith(".0")
                    ? s.substring(0, s.length() - 2)
                    : s;
            }
            default -> value.toString();
        };
    }

    // Stand in for Joe::println
    public static void println(String text) {
        System.out.println(text);
    }
}
