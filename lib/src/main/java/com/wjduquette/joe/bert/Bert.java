package com.wjduquette.joe.bert;

// Temporary main program.  Later this will be `BertEngine`, and we will
// invoke it from the main app.
public class Bert {
    //-------------------------------------------------------------------------
    // Stand-ins

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
