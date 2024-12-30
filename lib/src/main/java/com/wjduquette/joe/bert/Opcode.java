package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char CALL    = 1;
    public static final char CLASS   = 2;
    public static final char CLOSURE = 3;
    public static final char COMMENT = 4;
    public static final char CONST   = 5;
    public static final char DIV     = 6;
    public static final char EQ      = 7;
    public static final char FALSE   = 8;
    public static final char GE      = 9;
    public static final char GLODEF  = 10;
    public static final char GLOGET  = 11;
    public static final char GLOSET  = 12;
    public static final char GT      = 13;
    public static final char INHERIT = 14;
    public static final char JIF     = 15;
    public static final char JIFKEEP = 16;
    public static final char JITKEEP = 17;
    public static final char JUMP    = 18;
    public static final char LE      = 19;
    public static final char LOCGET  = 20;
    public static final char LOCSET  = 21;
    public static final char LOOP    = 22;
    public static final char LT      = 23;
    public static final char METHOD  = 24;
    public static final char MUL     = 25;
    public static final char NE      = 26;
    public static final char NEGATE  = 27;
    public static final char NOT     = 28;
    public static final char NULL    = 29;
    public static final char POP     = 30;
    public static final char POPN    = 31;
    public static final char PROPGET = 32;
    public static final char PROPSET = 33;
    public static final char RETURN  = 34;
    public static final char SUB     = 35;
    public static final char SUPGET  = 36;
    public static final char THROW   = 37;
    public static final char TRUE    = 38;
    public static final char UPCLOSE = 39;
    public static final char UPGET   = 40;
    public static final char UPSET   = 41;

    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "CALL",
        "CLASS",
        "CLOSURE",
        "COMMENT",
        "CONST",
        "DIV",
        "EQ",
        "FALSE",
        "GE",
        "GLODEF",
        "GLOGET",
        "GLOSET",
        "GT",
        "INHERIT",
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
        "POPN",
        "PROPGET",
        "PROPSET",
        "RETURN",
        "SUB",
        "SUPGET",
        "THROW",
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
