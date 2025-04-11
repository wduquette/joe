package com.wjduquette.joe.clark;

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
    /** Opcode */ public static final char DUP2    = 10;
    /** Opcode */ public static final char EQ      = 11;
    /** Opcode */ public static final char FALSE   = 12;
    /** Opcode */ public static final char GE      = 13;
    /** Opcode */ public static final char GETNEXT = 14;
    /** Opcode */ public static final char GLODEF  = 15;
    /** Opcode */ public static final char GLOGET  = 16;
    /** Opcode */ public static final char GLOLET  = 17;
    /** Opcode */ public static final char GLOSET  = 18;
    /** Opcode */ public static final char GT      = 19;
    /** Opcode */ public static final char HASNEXT = 20;
    /** Opcode */ public static final char IN      = 21;
    /** Opcode */ public static final char INCR    = 22;
    /** Opcode */ public static final char INDGET  = 23;
    /** Opcode */ public static final char INDSET  = 24;
    /** Opcode */ public static final char INHERIT = 25;
    /** Opcode */ public static final char ITER    = 26;
    /** Opcode */ public static final char JIF     = 27;
    /** Opcode */ public static final char JIFKEEP = 28;
    /** Opcode */ public static final char JIT     = 29;
    /** Opcode */ public static final char JITKEEP = 30;
    /** Opcode */ public static final char JUMP    = 31;
    /** Opcode */ public static final char LE      = 32;
    /** Opcode */ public static final char LISTADD = 33;
    /** Opcode */ public static final char LISTNEW = 34;
    /** Opcode */ public static final char LOCGET  = 35;
    /** Opcode */ public static final char LOCLET  = 36;
    /** Opcode */ public static final char LOCSET  = 37;
    /** Opcode */ public static final char LOOP    = 38;
    /** Opcode */ public static final char LT      = 39;
    /** Opcode */ public static final char MAPNEW  = 40;
    /** Opcode */ public static final char MAPPUT  = 41;
    /** Opcode */ public static final char MATCH   = 42;
    /** Opcode */ public static final char METHOD  = 43;
    /** Opcode */ public static final char MUL     = 44;
    /** Opcode */ public static final char NE      = 45;
    /** Opcode */ public static final char NEGATE  = 46;
    /** Opcode */ public static final char NI      = 47;
    /** Opcode */ public static final char NOT     = 48;
    /** Opcode */ public static final char NULL    = 49;
    /** Opcode */ public static final char POP     = 50;
    /** Opcode */ public static final char POPN    = 51;
    /** Opcode */ public static final char PROPGET = 52;
    /** Opcode */ public static final char PROPSET = 53;
    /** Opcode */ public static final char RECORD  = 54;
    /** Opcode */ public static final char RETURN  = 55;
    /** Opcode */ public static final char SUB     = 56;
    /** Opcode */ public static final char SUPGET  = 57;
    /** Opcode */ public static final char TGET    = 58;
    /** Opcode */ public static final char THROW   = 59;
    /** Opcode */ public static final char TPUT    = 60;
    /** Opcode */ public static final char TRCPOP  = 61;
    /** Opcode */ public static final char TRCPUSH = 62;
    /** Opcode */ public static final char TRUE    = 63;
    /** Opcode */ public static final char UPCLOSE = 64;
    /** Opcode */ public static final char UPGET   = 65;
    /** Opcode */ public static final char UPSET   = 66;

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
        "DUP2",
        "EQ",
        "FALSE",
        "GE",
        "GETNEXT",
        "GLODEF",
        "GLOGET",
        "GLOLET",
        "GLOSET",
        "GT",
        "HASNEXT",
        "IN",
        "INCR",
        "INDGET",
        "INDSET",
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
        "LOCLET",
        "LOCSET",
        "LOOP",
        "LT",
        "MAPNEW",
        "MAPPUT",
        "MATCH",
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
        "RECORD",
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
