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
    /** Opcode */ public static final char ADD     = 0;
    /** Opcode */ public static final char ASSERT  = 1;
    /** Opcode */ public static final char CALL    = 2;
    /** Opcode */ public static final char CLASS   = 3;
    /** Opcode */ public static final char CLOSURE = 4;
    /** Opcode */ public static final char COMMENT = 5;
    /** Opcode */ public static final char CONST   = 6;
    /** Opcode */ public static final char DECR    = 7;
    /** Opcode */ public static final char DIV     = 8;
    /** Opcode */ public static final char DUP     = 9;
    /** Opcode */ public static final char EQ      = 10;
    /** Opcode */ public static final char FALSE   = 11;
    /** Opcode */ public static final char GE      = 12;
    /** Opcode */ public static final char GETNEXT = 13;
    /** Opcode */ public static final char GLODEF  = 14;
    /** Opcode */ public static final char GLOGET  = 15;
    /** Opcode */ public static final char GLOSET  = 16;
    /** Opcode */ public static final char GT      = 17;
    /** Opcode */ public static final char HASNEXT = 18;
    /** Opcode */ public static final char IN      = 19;
    /** Opcode */ public static final char INCR    = 20;
    /** Opcode */ public static final char INHERIT = 21;
    /** Opcode */ public static final char ITER    = 22;
    /** Opcode */ public static final char JIF     = 23;
    /** Opcode */ public static final char JIFKEEP = 24;
    /** Opcode */ public static final char JIT     = 25;
    /** Opcode */ public static final char JITKEEP = 26;
    /** Opcode */ public static final char JUMP    = 27;
    /** Opcode */ public static final char LE      = 28;
    /** Opcode */ public static final char LISTADD = 29;
    /** Opcode */ public static final char LISTNEW = 30;
    /** Opcode */ public static final char LOCGET  = 31;
    /** Opcode */ public static final char LOCSET  = 32;
    /** Opcode */ public static final char LOOP    = 33;
    /** Opcode */ public static final char LT      = 34;
    /** Opcode */ public static final char METHOD  = 35;
    /** Opcode */ public static final char MUL     = 36;
    /** Opcode */ public static final char NE      = 37;
    /** Opcode */ public static final char NEGATE  = 38;
    /** Opcode */ public static final char NI      = 39;
    /** Opcode */ public static final char NOT     = 40;
    /** Opcode */ public static final char NULL    = 41;
    /** Opcode */ public static final char POP     = 42;
    /** Opcode */ public static final char POPN    = 43;
    /** Opcode */ public static final char PROPGET = 44;
    /** Opcode */ public static final char PROPSET = 45;
    /** Opcode */ public static final char RETURN  = 46;
    /** Opcode */ public static final char SUB     = 47;
    /** Opcode */ public static final char SUPGET  = 48;
    /** Opcode */ public static final char TGET    = 49;
    /** Opcode */ public static final char THROW   = 50;
    /** Opcode */ public static final char TPUT    = 51;
    /** Opcode */ public static final char TRCPOP  = 52;
    /** Opcode */ public static final char TRCPUSH = 53;
    /** Opcode */ public static final char TRUE    = 54;
    /** Opcode */ public static final char UPCLOSE = 55;
    /** Opcode */ public static final char UPGET   = 56;
    /** Opcode */ public static final char UPSET   = 57;

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
        "LISTADD",
        "LISTNEW",
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
