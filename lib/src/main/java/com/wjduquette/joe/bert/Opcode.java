package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char CALL    = 1;
    public static final char CLASS   = 2;
    public static final char CLOSURE = 3;
    public static final char CONST   = 4;
    public static final char DIV     = 5;
    public static final char EQ      = 6;
    public static final char FALSE   = 7;
    public static final char GE      = 8;
    public static final char GLODEF  = 9;
    public static final char GLOGET  = 10;
    public static final char GLOSET  = 11;
    public static final char GT      = 12;
    public static final char JIF     = 13;
    public static final char JIFKEEP = 14;
    public static final char JITKEEP = 15;
    public static final char JUMP    = 16;
    public static final char LE      = 17;
    public static final char LOCGET  = 18;
    public static final char LOCSET  = 19;
    public static final char LOOP    = 20;
    public static final char LT      = 21;
    public static final char METHOD  = 22;
    public static final char MUL     = 23;
    public static final char NE      = 24;
    public static final char NEGATE  = 25;
    public static final char NOT     = 26;
    public static final char NULL    = 27;
    public static final char POP     = 28;
    public static final char PROPGET = 29;
    public static final char PROPSET = 30;
    public static final char RETURN  = 31;
    public static final char SUB     = 32;
    public static final char TRUE    = 33;
    public static final char UPCLOSE = 34;
    public static final char UPGET   = 35;
    public static final char UPSET   = 36;

    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CALL",
        "CLASS",
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
        "METHOD",
        "MUL",
        "NE",
        "NEGATE",
        "NOT",
        "NULL",
        "POP",
        "PROPGET",
        "PROPSET",
        "RETURN",
        "SUB",
        "TRUE",
        "UPCLOSE",
        "UPGET",
        "UPSET"
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
