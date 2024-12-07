package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char CONST   = 1;
    public static final char DIV     = 2;
    public static final char EQ      = 3;
    public static final char FALSE   = 4;
    public static final char GE      = 5;
    public static final char GLODEF  = 6;
    public static final char GLOGET  = 7;
    public static final char GLOSET  = 8;
    public static final char GT      = 9;
    public static final char JIF     = 10;
    public static final char JIFKEEP = 11;
    public static final char JITKEEP = 12;
    public static final char JUMP    = 13;
    public static final char LE      = 14;
    public static final char LOCGET  = 15;
    public static final char LOCSET  = 16;
    public static final char LOOP    = 17;
    public static final char LT      = 18;
    public static final char MUL     = 19;
    public static final char NE      = 20;
    public static final char NEGATE  = 21;
    public static final char NOT     = 22;
    public static final char NULL    = 23;
    public static final char POP     = 24;
    public static final char RETURN  = 25;
    public static final char SUB     = 26;
    public static final char TRUE    = 27;

    // Temporary
    public static final char PRINT   = 28;


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
        "JIF",
        "JIFKEEP",
        "JITKEEP",
        "JUMP",
        "LE",
        "LOCGET",
        "LOCSET",
        "LOOP",
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
