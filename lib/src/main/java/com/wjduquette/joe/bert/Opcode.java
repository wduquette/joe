package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char CALL    = 1;
    public static final char CONST   = 2;
    public static final char DIV     = 3;
    public static final char EQ      = 4;
    public static final char FALSE   = 5;
    public static final char GE      = 6;
    public static final char GLODEF  = 7;
    public static final char GLOGET  = 8;
    public static final char GLOSET  = 9;
    public static final char GT      = 10;
    public static final char JIF     = 11;
    public static final char JIFKEEP = 12;
    public static final char JITKEEP = 13;
    public static final char JUMP    = 14;
    public static final char LE      = 15;
    public static final char LOCGET  = 16;
    public static final char LOCSET  = 17;
    public static final char LOOP    = 18;
    public static final char LT      = 19;
    public static final char MUL     = 20;
    public static final char NE      = 21;
    public static final char NEGATE  = 22;
    public static final char NOT     = 23;
    public static final char NULL    = 24;
    public static final char POP     = 25;
    public static final char RETURN  = 26;
    public static final char SUB     = 27;
    public static final char TRUE    = 28;

    // Temporary
    public static final char PRINT   = 29;


    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CALL",
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
