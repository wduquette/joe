package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to format strings for Joe.
 */
public class StringFormatter {
    private StringFormatter() {} // Not instantiable.
    private static final Map<String,List<ArgType>> formatCache = new HashMap<>();

    /**
     * Formats a string in the Joe context, validating arguments and converting
     * doubles to integers as needed.  See the Joe User's Guide for
     * constraints on the format string.  The format string is parsed to the
     * extent needed to:
     *
     * <ul>
     * <li>Exclude unsupported conversions, etc.</li>
     * <li>Determine the expected types of the values to format.</li>
     * </ul>
     *
     * <p>For the numeric conversions, the code expects Doubles or
     * Integers; it does not support any other numeric types.</p>
     *
     * <p>Note: if it weren't for the double-to-integer conversion, we
     * could just call `String.format()` and throw any exception as a
     * JoeError.  But the feature would be much less useful that way.</p>
     * @param joe The Joe instance.
     * @param fmt The format string
     * @param args The arguments
     * @return The formatted string.
     */
    public static String format(
        Joe joe,
        String fmt,
        List<Object> args
    ) {
        // FIRST, parse the format for the argument types, caching the
        // result for later.
        var types = formatCache.computeIfAbsent(fmt, f -> new Parser(f).parse());

        // NEXT, do we have the right number of arguments?
        if (types.size() != args.size()) {
            throw new JoeError("Expected " + types.size() +
                " values to format, got: " + args.size() + ".");
        }

        // NEXT, build the array of values, doing any needed checks and
        // conversions.
        var values = new Object[types.size()];
        for (var i = 0; i < types.size(); i++) {
            var arg = args.get(i);
            values[i] = switch (types.get(i)) {
                case ANY -> arg;
                case STRING -> joe.stringify(arg);
                case DOUBLE -> {
                    if (arg instanceof Double) {
                        yield arg;
                    } else if (arg instanceof Integer num) {
                        yield (double)num;
                    }
                    throw new JoeError("Conversion expected a number, got: " +
                            joe.typedValue(arg) + ".")
                        .addInfo("In format '" + fmt + "'.");
                }
                case INT -> {
                    if (arg instanceof Double d) {
                        yield d.intValue();
                    } else if (arg instanceof Integer) {
                        yield arg;
                    }
                    throw new JoeError("Conversion expected a number, got: " +
                            joe.typedValue(arg) + ".")
                        .addInfo("In format '" + fmt + "'.");
                }
            };
        }

        // NEXT, format the string, rethrowing any format errors.
        try {
            return String.format(fmt, values);
        } catch (Exception ex) {
            throw new JoeError("Invalid format string: '" + fmt + "'.");
        }
    }

    //-------------------------------------------------------------------------
    // Format Parser

    // The Java type expected by a conversion in the format string:
    // Any value at all, a string, a double, or an int.
    private enum ArgType {
        ANY,
        STRING,
        DOUBLE,
        INT
    }

    private static class Parser {
        private final String source;
        private int current = 0;

        Parser(String source) {
            this.source = source;
        }

        private List<ArgType> parse() {
            var result = new ArrayList<ArgType>();

            var inConversion = false;
            while (!isAtEnd()) {
                var c = advance();

                if (inConversion) {
                    switch (c) {
                        // Conversions; save the arg type (if any)
                        case 'b', 'B', 'h', 'H' -> {
                            result.add(ArgType.ANY);
                            inConversion = false;
                        }
                        case 's', 'S' -> {
                            result.add(ArgType.STRING);
                            inConversion = false;
                        }
                        case 'd', 'x', 'X' -> {
                            result.add(ArgType.INT);
                            inConversion = false;
                        }
                        case 'e', 'E', 'f', 'g', 'G' -> {
                            result.add(ArgType.DOUBLE);
                            inConversion = false;
                        }
                        case 'n', '%' -> inConversion = false;

                        // Valid characters within a conversion.
                        case '-', '+', ' ', ',', '(', '.',
                             '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                            -> {}

                        // Characters that shouldn't be with a Joe conversion.
                        default -> throw new JoeError(
                            "Invalid character in conversion: '" + c + "'.");
                    }
                } else {
                    if (c == '%') inConversion = true;
                }
            }

            return result;
        }

        private boolean isAtEnd() {
            return current >= source.length();
        }

        private char advance() {
            return source.charAt(current++);
        }
    }
}
