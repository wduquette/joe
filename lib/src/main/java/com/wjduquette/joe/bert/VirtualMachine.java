package com.wjduquette.joe.bert;

import static com.wjduquette.joe.bert.Opcode.*;

class VirtualMachine {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The current chunk.  Later, this will be a `Function`.
    private Chunk chunk = null;
    private int ip;

    //-------------------------------------------------------------------------
    // Constructor

    VirtualMachine() {
    }

    //-------------------------------------------------------------------------
    // Methods

    void interpret(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0;
        run();
    }

    // At present this uses Chunk directly.  Later the chunk info will
    // be in `Function` in a more efficient form.
    private void run() {
        for (;;) {
            var opcode = chunk.code(ip++);
            switch (opcode) {
                case CONSTANT -> {
                    var constant = readConstant();
                    Bert.println(Bert.stringify(constant));
                }
                case RETURN -> { return; }
                default -> throw new IllegalStateException(
                    "Unknown opcode: " + opcode + ".");
            }
        }
    }

    // Reads a constant index from the chunk, and returns the indexed
    // constant.
    private Object readConstant() {
        var index = chunk.code(ip++);
        return chunk.getConstant(index);
    }
}
