package com.wjduquette.joe.bert;

public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    public static final char ADD     = 0;
    public static final char ASSERT  = 1;
    public static final char CALL    = 2;
    public static final char CLASS   = 3;
    public static final char CLOSURE = 4;
    public static final char COMMENT = 5;
    public static final char CONST   = 6;
    public static final char DIV     = 7;
    public static final char EQ      = 8;
    public static final char FALSE   = 9;
    public static final char GE      = 10;
    public static final char GLODEF  = 11;
    public static final char GLOGET  = 12;
    public static final char GLOSET  = 13;
    public static final char GT      = 14;
    public static final char INHERIT = 15;
    public static final char JIF     = 16;
    public static final char JIFKEEP = 17;
    public static final char JITKEEP = 18;
    public static final char JUMP    = 19;
    public static final char LE      = 20;
    public static final char LOCGET  = 21;
    public static final char LOCSET  = 22;
    public static final char LOOP    = 23;
    public static final char LT      = 24;
    public static final char METHOD  = 25;
    public static final char MUL     = 26;
    public static final char NE      = 27;
    public static final char NEGATE  = 28;
    public static final char NOT     = 29;
    public static final char NULL    = 30;
    public static final char POP     = 31;
    public static final char POPN    = 32;
    public static final char PROPGET = 33;
    public static final char PROPSET = 34;
    public static final char RETURN  = 35;
    public static final char SUB     = 36;
    public static final char SUPGET  = 37;
    public static final char THROW   = 38;
    public static final char TRUE    = 39;
    public static final char UPCLOSE = 40;
    public static final char UPGET   = 41;
    public static final char UPSET   = 42;

    //-------------------------------------------------------------------------
    // Opcode names

    private static final String[] names = {
        "ADD",
        "ASSERT",
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
