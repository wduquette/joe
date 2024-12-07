package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD    = 0;
    public static final char CONST  = 1;
    public static final char DIV    = 2;
    public static final char EQ     = 3;
    public static final char FALSE  = 4;
    public static final char GE     = 5;
    public static final char GLODEF = 6;
    public static final char GLOGET = 7;
    public static final char GLOSET = 8;
    public static final char GT     = 9;
    public static final char LE     = 10;
    public static final char LOCGET = 11;
    public static final char LOCSET = 12;
    public static final char LT     = 13;
    public static final char MUL    = 14;
    public static final char NE     = 15;
    public static final char NEGATE = 16;
    public static final char NOT    = 17;
    public static final char NULL   = 18;
    public static final char POP    = 19;
    public static final char RETURN = 20;
    public static final char SUB    = 21;
    public static final char TRUE   = 22;

    // Temporary
    public static final char PRINT  = 23;


    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CONST",
        "DIV",
        "EQ",
        "FALSE",
        "GE",
        "GLODEF",
        "GLOGET",
        "GLOSET",
        "GT",
        "LE",
        "LOCGET",
        "LOCSET",
        "LT",
        "MUL",
        "NE",
        "NEGATE",
        "NOT",
        "NULL",
        "POP",
        "RETURN",
        "SUB",
        "TRUE",

        // Temporary
        "PRINT"
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
