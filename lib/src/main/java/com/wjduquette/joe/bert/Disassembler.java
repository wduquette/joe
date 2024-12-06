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
            buff.append(chunkPrefix(ip))
                .append(pair.result)
                .append("\n");
            ip = pair.next;
        }

        return buff.toString();
    }

    String disassembleInstruction(Chunk chunk, int ip) {
        this.chunk = chunk;
        return singlePrefix(ip) + instruction(ip).result;
    }

    private Pair instruction(int ip) {
        var opcode = chunk.code(ip);
        return switch (opcode) {
            case CONSTANT -> constantInstruction(ip);
            case RETURN -> simpleInstruction(ip);
            default -> unknownOpcode(ip);
        };
    }

    private Pair constantInstruction(int ip) {
        int index = chunk.code(ip + 1);
        var constant = chunk.getConstant(index);
        var text = String.format(" %04d '%s'",
            index, Bert.stringify(constant));
        return new Pair(text, ip + 2);
    }

    private Pair simpleInstruction(int ip) {
        return new Pair("", ip + 1);
    }

    private Pair unknownOpcode(int ip) {
        var opcode = chunk.code(ip);
        var text = String.format(" %03d", (int)opcode);
        return new Pair(text, ip + 1);
    }

    private String singlePrefix(int ip) {
        char opcode = chunk.code(ip);
        return String.format("%04d @%04d %-9s",
            chunk.line(ip), ip, Opcode.name(opcode));
    }

    private String chunkPrefix(int ip) {
        String line;
        char opcode = chunk.code(ip);

        if (ip > 0 && chunk.line(ip) == chunk.line(ip - 1)) {
            line = "   | ";
        } else {
            line = String.format("%04d ", chunk.line(ip));
        }

        return String.format("%s @%04d %-9s", line, ip, Opcode.name(opcode));
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The result of disassembling a single instruction.
    private record Pair(String result, int next) { }
}
