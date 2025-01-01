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
    public static final char GETNEXT = 13;
    public static final char GLODEF  = 14;
    public static final char GLOGET  = 15;
    public static final char GLOSET  = 16;
    public static final char GT      = 17;
    public static final char HASNEXT = 18;
    public static final char IN      = 19;
    public static final char INCR    = 20;
    public static final char INHERIT = 21;
    public static final char ITER    = 22;
    public static final char JIF     = 23;
    public static final char JIFKEEP = 24;
    public static final char JITKEEP = 25;
    public static final char JUMP    = 26;
    public static final char LE      = 27;
    public static final char LOCGET  = 28;
    public static final char LOCSET  = 29;
    public static final char LOOP    = 30;
    public static final char LT      = 31;
    public static final char METHOD  = 32;
    public static final char MUL     = 33;
    public static final char NE      = 34;
    public static final char NEGATE  = 35;
    public static final char NI      = 36;
    public static final char NOT     = 37;
    public static final char NULL    = 38;
    public static final char POP     = 39;
    public static final char POPN    = 40;
    public static final char PROPGET = 41;
    public static final char PROPSET = 42;
    public static final char RETURN  = 43;
    public static final char SUB     = 44;
    public static final char SUPGET  = 45;
    public static final char TGET    = 46;
    public static final char THROW   = 47;
    public static final char TPUT    = 48;
    public static final char TRUE    = 49;
    public static final char UPCLOSE = 50;
    public static final char UPGET   = 51;
    public static final char UPSET   = 52;

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
        "GETNEXT",
        "GLODEF",
        "GLOGET",
        "GLOSET",
        "GT",
        "HASNEXT",
        "IN",
        "INCR",
        "INHERIT",
        "ITER",
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
