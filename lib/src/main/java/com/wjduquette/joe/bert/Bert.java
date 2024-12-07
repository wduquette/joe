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

    public static boolean isFalsey(Object value) {
        return !Joe.isTruthy(value);
    }

    // Stand in for BertEngine::isDebug
    public static boolean isDebug() {
        return true;
    }

    // Stand in for Joe::stringify
    public static String stringify(Object value) {
        return switch (value) {
            case null -> "null";
            default -> value.toString();
        };
    }

    // Stand in for Joe::println
    public static void println(String text) {
        System.out.println(text);
    }
}
