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

    String disassemble(String name, Chunk chunk) {
        this.chunk = chunk;
        var buff = new StringBuilder();

        buff.append("== ").append(name).append("==\n");

        for (int ip = 0; ip < chunk.size(); ) {
            var pair = instruction(ip);
            buff.append(pair.result).append("\n");
            ip = pair.next;
        }

        return buff.toString();
    }

    String disassembleInstruction(Chunk chunk, int offset) {
        this.chunk = chunk;
        return instruction(offset).result;
    }

    private Pair instruction(int ip) {
        var opcode = chunk.get(ip);
        return switch (opcode) {
            case RETURN -> simpleInstruction(opcode, ip);
            default -> unknownOpcode(opcode, ip);
        };
    }

    private Pair simpleInstruction(char opcode, int ip) {
        var text = String.format("%04d %s", ip, Opcode.name(opcode));
        return new Pair(text, ip + 1);
    }

    private Pair unknownOpcode(char opcode, int ip) {
        var text = String.format("%04d Unknown opcode %03d", ip, opcode);
        return new Pair(text, ip + 1);
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The result of disassembling a single instruction.
    private record Pair(String result, int next) { }
}
