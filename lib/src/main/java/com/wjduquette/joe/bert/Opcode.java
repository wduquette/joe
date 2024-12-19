package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char CALL    = 1;
    public static final char CLOSURE = 2;
    public static final char CONST   = 3;
    public static final char DIV     = 4;
    public static final char EQ      = 5;
    public static final char FALSE   = 6;
    public static final char GE      = 7;
    public static final char GLODEF  = 8;
    public static final char GLOGET  = 9;
    public static final char GLOSET  = 10;
    public static final char GT      = 11;
    public static final char JIF     = 12;
    public static final char JIFKEEP = 13;
    public static final char JITKEEP = 14;
    public static final char JUMP    = 15;
    public static final char LE      = 16;
    public static final char LOCGET  = 17;
    public static final char LOCSET  = 18;
    public static final char LOOP    = 19;
    public static final char LT      = 20;
    public static final char MUL     = 21;
    public static final char NE      = 22;
    public static final char NEGATE  = 23;
    public static final char NOT     = 24;
    public static final char NULL    = 25;
    public static final char POP     = 26;
    public static final char RETURN  = 27;
    public static final char SUB     = 28;
    public static final char TRUE    = 29;

    // Temporary
    public static final char PRINT   = 30;


    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CALL",
        "CLOSURE",
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
