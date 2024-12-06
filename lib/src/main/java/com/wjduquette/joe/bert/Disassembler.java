package com.wjduquette.joe.bert;

import static com.wjduquette.joe.bert.Opcode.*;

/**
 * A disassembler for compiled Bert code.
 */
public class Disassembler {
    //-------------------------------------------------------------------------
    // Instance Variables

    private transient Chunk chunk;

    //-------------------------------------------------------------------------
    // Constructor

    public Disassembler() {
        // Nothing to do
    }

    //-------------------------------------------------------------------------
    // Methods

    void disassemble(String name, Chunk chunk) {
        this.chunk = chunk;

        System.out.println("== " + name + "==");

        for (int ip = 0; ip < chunk.size(); ) {
            ip = instruction(ip);
        }
    }

    int disassembleInstruction(Chunk chunk, int offset) {
        this.chunk = chunk;
        return instruction(offset);
    }

    private int instruction(int ip) {
        System.out.printf("%04d ", ip);

        var opcode = chunk.get(ip);
        return switch (opcode) {
            case RETURN -> simpleInstruction(opcode, ip);
            default -> {
                System.out.println("Unknown opcode: " + opcode + ".");
                yield ip + 1;
            }
        };
    }

    private int simpleInstruction(char opcode, int ip) {
        System.out.println(Opcode.name(opcode));
        return ip + 1;
    }
}
