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
    public static final char DECR    = 7;
    public static final char DIV     = 8;
    public static final char DUP     = 9;
    public static final char EQ      = 10;
    public static final char FALSE   = 11;
    public static final char GE      = 12;
    public static final char GLODEF  = 13;
    public static final char GLOGET  = 14;
    public static final char GLOSET  = 15;
    public static final char GT      = 16;
    public static final char IN      = 17;
    public static final char INCR    = 18;
    public static final char INHERIT = 19;
    public static final char JIF     = 20;
    public static final char JIFKEEP = 21;
    public static final char JITKEEP = 22;
    public static final char JUMP    = 23;
    public static final char LE      = 24;
    public static final char LOCGET  = 25;
    public static final char LOCSET  = 26;
    public static final char LOOP    = 27;
    public static final char LT      = 28;
    public static final char METHOD  = 29;
    public static final char MUL     = 30;
    public static final char NE      = 31;
    public static final char NEGATE  = 32;
    public static final char NI      = 33;
    public static final char NOT     = 34;
    public static final char NULL    = 35;
    public static final char POP     = 36;
    public static final char POPN    = 37;
    public static final char PROPGET = 38;
    public static final char PROPSET = 39;
    public static final char RETURN  = 40;
    public static final char SUB     = 41;
    public static final char SUPGET  = 42;
    public static final char TGET    = 43;
    public static final char THROW   = 44;
    public static final char TPUT    = 45;
    public static final char TRUE    = 46;
    public static final char UPCLOSE = 47;
    public static final char UPGET   = 48;
    public static final char UPSET   = 49;

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
        "DECR",
        "DIV",
        "DUP",
        "EQ",
        "FALSE",
        "GE",
        "GLODEF",
        "GLOGET",
        "GLOSET",
        "GT",
        "IN",
        "INCR",
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
        "NI",
        "NOT",
        "NULL",
        "POP",
        "POPN",
        "PROPGET",
        "PROPSET",
        "RETURN",
        "SUB",
        "SUPGET",
        "TGET",
        "THROW",
        "TPUT",
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
