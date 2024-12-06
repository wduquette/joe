package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD = 0;
    public static final char CONST = 1;
    public static final char DIV = 2;
    public static final char MUL = 3;
    public static final char NEGATE = 4;
    public static final char RETURN = 5;
    public static final char SUB = 6;

    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CONST",
        "DIV",
        "MUL",
        "NEGATE",
        "RETURN",
        "SUB"
    };

    //-------------------------------------------------------------------------
    // API

    /**
     * Gets the name of the given opcode.
     * @param opcode The opcode
     * @return The name
     */
    public static String name(char opcode) {
        return opcode <= names.length
            ? names[opcode] : "Unknown";
    }
}
