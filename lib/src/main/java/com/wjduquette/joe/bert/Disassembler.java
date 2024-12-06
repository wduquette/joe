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

        for (int ip = 0; ip < chunk.codeSize(); ) {
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
            case CONSTANT -> constantInstruction(opcode, ip);
            case RETURN -> simpleInstruction(opcode, ip);
            default -> unknownOpcode(opcode, ip);
        };
    }

    // TODO: Need to use stringify().
    private Pair constantInstruction(char opcode, int ip) {
        int index = chunk.get(ip + 1);
        var constant = chunk.getConstant(index);
        var text = String.format("%04d %-9s %04d '%s'",
            ip, Opcode.name(opcode), index, constant.toString());
        return new Pair(text, ip + 2);
    }

    private Pair simpleInstruction(char opcode, int ip) {
        var text = String.format("%04d %s", ip, Opcode.name(opcode));
        return new Pair(text, ip + 1);
    }

    private Pair unknownOpcode(char opcode, int ip) {
        var text = String.format("%04d Unknown opcode %03d",
            ip, (int)opcode);
        return new Pair(text, ip + 1);
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The result of disassembling a single instruction.
    private record Pair(String result, int next) { }
}
