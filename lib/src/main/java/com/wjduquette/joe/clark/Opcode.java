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
    /** Opcode */ public static final char GLOBIND = 15;
    /** Opcode */ public static final char GLODEF  = 16;
    /** Opcode */ public static final char GLOGET  = 17;
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
    /** Opcode */ public static final char LOCBIND = 35;
    /** Opcode */ public static final char LOCGET  = 36;
    /** Opcode */ public static final char LOCMOVE = 37;
    /** Opcode */ public static final char LOCSET  = 38;
    /** Opcode */ public static final char LOOP    = 39;
    /** Opcode */ public static final char LT      = 40;
    /** Opcode */ public static final char MAPNEW  = 41;
    /** Opcode */ public static final char MAPPUT  = 42;
    /** Opcode */ public static final char MATCH   = 43;
    /** Opcode */ public static final char MATCHG  = 44;
    /** Opcode */ public static final char MATCHL  = 45;
    /** Opcode */ public static final char METHOD  = 46;
    /** Opcode */ public static final char MUL     = 47;
    /** Opcode */ public static final char NE      = 48;
    /** Opcode */ public static final char NEGATE  = 49;
    /** Opcode */ public static final char NI      = 50;
    /** Opcode */ public static final char NOT     = 51;
    /** Opcode */ public static final char NULL    = 52;
    /** Opcode */ public static final char PATTERN = 53;
    /** Opcode */ public static final char POP     = 54;
    /** Opcode */ public static final char POPN    = 55;
    /** Opcode */ public static final char PROPGET = 56;
    /** Opcode */ public static final char PROPSET = 57;
    /** Opcode */ public static final char RECORD  = 58;
    /** Opcode */ public static final char RETURN  = 59;
    /** Opcode */ public static final char RULESET = 60;
    /** Opcode */ public static final char SETADD  = 61;
    /** Opcode */ public static final char SETNEW  = 62;
    /** Opcode */ public static final char SUB     = 63;
    /** Opcode */ public static final char SUPGET  = 64;
    /** Opcode */ public static final char SWAP    = 65;
    /** Opcode */ public static final char TGET    = 66;
    /** Opcode */ public static final char THROW   = 67;
    /** Opcode */ public static final char TPUT    = 68;
    /** Opcode */ public static final char TRCPOP  = 69;
    /** Opcode */ public static final char TRCPUSH = 70;
    /** Opcode */ public static final char TSET    = 71;
    /** Opcode */ public static final char TRUE    = 72;
    /** Opcode */ public static final char UPCLOSE = 73;
    /** Opcode */ public static final char UPGET   = 74;
    /** Opcode */ public static final char UPSET   = 75;

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
        "GLOBIND",
        "GLODEF",
        "GLOGET",
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
        "LOCBIND",
        "LOCGET",
        "LOCMOVE",
        "LOCSET",
        "LOOP",
        "LT",
        "MAPNEW",
        "MAPPUT",
        "MATCH",
        "MATCHG",
        "MATCHL",
        "METHOD",
        "MUL",
        "NE",
        "NEGATE",
        "NI",
        "NOT",
        "NULL",
        "PATTERN",
        "POP",
        "POPN",
        "PROPGET",
        "PROPSET",
        "RECORD",
        "RETURN",
        "RULESET",
        "SETADD",
        "SETNEW",
        "SUB",
        "SUPGET",
        "SWAP",
        "TGET",
        "THROW",
        "TPUT",
        "TRCPOP",
        "TRCPUSH",
        "TSET",
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
