package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;

import java.util.ArrayList;

import static com.wjduquette.joe.bert.Opcode.*;

/**
 * A disassembler for compiled Bert code.
 */
public class Disassembler {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private transient CodeChunk chunk;

    //-------------------------------------------------------------------------
    // Constructor

    public Disassembler(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Methods

    String disassemble(CodeChunk chunk) {
        this.chunk = chunk;
        // FIRST, get the title and constants table.
        var buff = new StringBuilder();

        buff.append("=== ")
            .append(chunk.type())
            .append(" ")
            .append(chunk.name()).append(" ===\n");

        var compressed = RLE.encodedLength(chunk.lines());
        var full = (double)chunk.codeSize();
        var percentage = 100.0*compressed/full;
        buff.append("Lines RLE size: ")
            .append(String.format("%d/%d = %.1f%%",
                compressed, chunk.codeSize(), percentage))
            .append("\n");

        for (int i = 0; i < chunk.numConstants(); i++) {
            buff.append("[")
                .append(i)
                .append("] = '")
                .append(joe.stringify(chunk.getConstant(i)))
                .append("'\n");
        }

        if (chunk.numConstants() > 0) {
            buff.append("\n");
        }

        // NEXT, get the two data columns
        var left = new ArrayList<String>();
        var right = new ArrayList<String>();

        var source = chunk.source();
        left.add(String.format("%04d", chunk.span().startLine()));
        right.add(source.line(chunk.span().startLine()));

        for (int ip = 0; ip < chunk.codeSize(); ) {
            if (chunk.source() != null && startsNewLine(ip)) {
                right.add(source.line(chunk.line(ip)));
            } else {
                right.add("");
            }
            var pair = instruction(ip);
            left.add(chunkPrefix(ip) + pair.result);

            ip = pair.next;
        }

        var leftWidth = left.stream()
            .mapToInt(String::length)
            .max()
            .orElse(0);

        for (var i = 0; i < left.size(); i++) {
            if (right.get(i).isEmpty()) {
                buff.append(left.get(i))
                    .append("\n");
            } else {
                buff.append(pad(left.get(i), leftWidth))
                    .append(" ; ")
                    .append(right.get(i))
                    .append("\n");
            }
        }

        return buff.toString();
    }

    private String pad(String text, int width) {
        return text.length() >= width
            ? text
            : text + " ".repeat(width - text.length());
    }

    String disassembleInstruction(CodeChunk chunk, int ip) {
        this.chunk = chunk;
        return singlePrefix(ip) + instruction(ip).result;
    }

    private Pair instruction(int ip) {
        var opcode = chunk.code(ip);
        return switch (opcode) {
            case CALL, LOCGET, LOCSET
                -> charInstruction(ip);
            case JIF, JIFKEEP, JITKEEP, JUMP
                -> jumpInstruction(ip, 1);
            case LOOP
                -> jumpInstruction(ip, -1);
            case CONST, GLODEF, GLOGET, GLOSET
                -> constantInstruction(ip);
            case ADD, DIV, EQ, FALSE, GE, GT, LE, LT, MUL,
                NE, NEGATE, NOT, NULL, POP, PRINT, RETURN, SUB, TRUE
                -> simpleInstruction(ip);
            case CLOSURE -> closureInstruction(ip);
            default -> unknownOpcode(ip);
        };
    }

    private Pair charInstruction(int ip) {
        char arg = chunk.code(ip + 1);
        var text = String.format(" %04d", (int)arg);
        return new Pair(text, ip + 2);
    }

    private Pair closureInstruction(int ip) {
        int index = chunk.code(ip + 1);
        var func = (Function)chunk.getConstant(index);
        var text = String.format(" %04d '%s'", index, func.toString());
        return new Pair(text, ip + 2);
    }


    private Pair constantInstruction(int ip) {
        int index = chunk.code(ip + 1);
        var constant = joe.stringify(chunk.getConstant(index));
        if (constant.length() > 10) {
            constant = constant.substring(0, 7) + "...";
        }
        var text = String.format(" %04d '%s'", index, constant);
        return new Pair(text, ip + 2);
    }

    private Pair jumpInstruction(int ip, int sign) {
        char jump = chunk.code(ip + 1);
        var text = String.format(" %04d -> %d", (int)jump,
            ip + 2 + sign*jump);
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
        return String.format("%04d @%04d %-6s",
            chunk.line(ip), ip, Opcode.name(opcode));
    }

    private String chunkPrefix(int ip) {
        String line;
        char opcode = chunk.code(ip);

        if (startsNewLine(ip)) {
            line = String.format("%04d ", chunk.line(ip));
        } else {
            line = "   | ";
        }

        return String.format("%s @%04d %-6s", line, ip, Opcode.name(opcode));
    }

    private boolean startsNewLine(int ip) {
        return ip == 0 || chunk.line(ip) != chunk.line(ip - 1);
    }

    //-------------------------------------------------------------------------
    // Helpers

    // The result of disassembling a single instruction.
    private record Pair(String result, int next) { }
}
