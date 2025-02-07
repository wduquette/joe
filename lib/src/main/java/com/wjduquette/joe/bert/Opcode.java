package com.wjduquette.joe.bert;

/**
 * The instruction opcodes for Bert's {@link VirtualMachine}. Opcodes
 * are represented as {@code char} constants, as that's the form they
 * take in a {@link CodeChunk}, and anything else slows down compilation
 * and execution.
 */
public class Opcode {
    private Opcode() {} // Not instantiable

    //-------------------------------------------------------------------------
    // Opcode Definitions

    // Every opcode must appear in this table with a unique numeric constant.
    // Opcodes should be in alphabetical order, and the related constants
    // should be consecutive starting at 0 so that the constants and the
    // {@code names} array are consistent.
    //
    // When adding an opcode, be sure to add its name to the {@code names}
    // array, below.
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
    public static final char JIT     = 25;
    public static final char JITKEEP = 26;
    public static final char JUMP    = 27;
    public static final char LE      = 28;
    public static final char LOCGET  = 29;
    public static final char LOCSET  = 30;
    public static final char LOOP    = 31;
    public static final char LT      = 32;
    public static final char METHOD  = 33;
    public static final char MUL     = 34;
    public static final char NE      = 35;
    public static final char NEGATE  = 36;
    public static final char NI      = 37;
    public static final char NOT     = 38;
    public static final char NULL    = 39;
    public static final char POP     = 40;
    public static final char POPN    = 41;
    public static final char PROPGET = 42;
    public static final char PROPSET = 43;
    public static final char RETURN  = 44;
    public static final char SUB     = 45;
    public static final char SUPGET  = 46;
    public static final char TGET    = 47;
    public static final char THROW   = 48;
    public static final char TPUT    = 49;
    public static final char TRCPOP  = 50;
    public static final char TRCPUSH = 51;
    public static final char TRUE    = 52;
    public static final char UPCLOSE = 53;
    public static final char UPGET   = 54;
    public static final char UPSET   = 55;

    //-------------------------------------------------------------------------
    // Opcode names

    // The name of each opcode, for use by the disassembler.
    // The opcode itself is the index into this array; be sure to add
    // a name for each added opcode.  Names and opcodes should both be
    // listed in alphabetical order.
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
        "JIT",
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
        "TRCPOP",
        "TRCPUSH",
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
