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
    /** Opcode */ public static final char CELLGET = 3;
    /** Opcode */ public static final char CELLSET = 4;
    /** Opcode */ public static final char CLASS   = 5;
    /** Opcode */ public static final char CLOSURE = 6;
    /** Opcode */ public static final char COMMENT = 7;
    /** Opcode */ public static final char CONST   = 8;
    /** Opcode */ public static final char DECR    = 9;
    /** Opcode */ public static final char DIV     = 10;
    /** Opcode */ public static final char DUP     = 11;
    /** Opcode */ public static final char EQ      = 12;
    /** Opcode */ public static final char FALSE   = 13;
    /** Opcode */ public static final char GE      = 14;
    /** Opcode */ public static final char GETNEXT = 15;
    /** Opcode */ public static final char GLODEF  = 16;
    /** Opcode */ public static final char GLOGET  = 17;
    /** Opcode */ public static final char GLOSET  = 18;
    /** Opcode */ public static final char GT      = 19;
    /** Opcode */ public static final char HASNEXT = 20;
    /** Opcode */ public static final char IN      = 21;
    /** Opcode */ public static final char INCR    = 22;
    /** Opcode */ public static final char INHERIT = 23;
    /** Opcode */ public static final char ITER    = 24;
    /** Opcode */ public static final char JIF     = 25;
    /** Opcode */ public static final char JIFKEEP = 26;
    /** Opcode */ public static final char JIT     = 27;
    /** Opcode */ public static final char JITKEEP = 28;
    /** Opcode */ public static final char JUMP    = 29;
    /** Opcode */ public static final char LE      = 30;
    /** Opcode */ public static final char LISTADD = 31;
    /** Opcode */ public static final char LISTNEW = 32;
    /** Opcode */ public static final char LOCGET  = 33;
    /** Opcode */ public static final char LOCSET  = 34;
    /** Opcode */ public static final char LOOP    = 35;
    /** Opcode */ public static final char LT      = 36;
    /** Opcode */ public static final char MAPNEW  = 37;
    /** Opcode */ public static final char MAPPUT  = 38;
    /** Opcode */ public static final char METHOD  = 39;
    /** Opcode */ public static final char MUL     = 40;
    /** Opcode */ public static final char NE      = 41;
    /** Opcode */ public static final char NEGATE  = 42;
    /** Opcode */ public static final char NI      = 43;
    /** Opcode */ public static final char NOT     = 44;
    /** Opcode */ public static final char NULL    = 45;
    /** Opcode */ public static final char POP     = 46;
    /** Opcode */ public static final char POPN    = 47;
    /** Opcode */ public static final char PROPCEL = 48;
    /** Opcode */ public static final char PROPGET = 49;
    /** Opcode */ public static final char PROPSET = 50;
    /** Opcode */ public static final char RETURN  = 51;
    /** Opcode */ public static final char SUB     = 52;
    /** Opcode */ public static final char SUPGET  = 53;
    /** Opcode */ public static final char TGET    = 54;
    /** Opcode */ public static final char THROW   = 55;
    /** Opcode */ public static final char TPUT    = 56;
    /** Opcode */ public static final char TRCPOP  = 57;
    /** Opcode */ public static final char TRCPUSH = 58;
    /** Opcode */ public static final char TRUE    = 59;
    /** Opcode */ public static final char UPCLOSE = 60;
    /** Opcode */ public static final char UPGET   = 61;
    /** Opcode */ public static final char UPSET   = 62;

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
        "CELLGET",
        "CELLSET",
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
        "MAPNEW",
        "MAPPUT",
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
