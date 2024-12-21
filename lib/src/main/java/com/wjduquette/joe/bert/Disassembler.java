package com.wjduquette.joe.bert;

import com.wjduquette.joe.Joe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        // NEXT, get the lines of the output.
        var lines = new ArrayList<Line>();
        for (int ip = 0; ip < chunk.codeSize(); ) {
            ip = decode(lines, ip, true);
        }

        // NEXT, get the width of the left column.
        var leftMax = lines.stream()
            .map(Line::text)
            .mapToInt(String::length)
            .max()
            .orElse(0);
        var leftWidth = Math.max(leftMax, 40);

        // NEXT, output each line with its source.
        var source = chunk.source();
        buff.append(pad(String.format("%04d", chunk.span().startLine()), leftWidth))
            .append("  ")
            .append(source.line(chunk.span().startLine()))
            .append("\n");

        for (var line : lines) {
            buff.append(pad(line.text, leftWidth));

            if (startsNewLine(line.ip)) {
                buff.append("  ")
                    .append(source.line(chunk.line(line.ip)));
            }
            buff.append("\n");
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
        var lines = new ArrayList<Line>();
        decode(lines, ip, false);
        return lines.stream()
            .map(Line::text)
            .collect(Collectors.joining("\n"));
    }


    // Decodes one or more lines starting at ip. Returns the next
    // ip to decode.
    private int decode(List<Line> lines, int ip, boolean isChunk) {
        var opcode = chunk.code(ip);
        var prefix = isChunk ? chunkPrefix(ip) : singlePrefix(ip);

        switch (opcode) {
            case CALL, LOCGET, LOCSET, UPGET, UPSET -> {
                // Pattern: opcode charValue
                char arg = chunk.code(ip + 1);
                var text = String.format(" %04d", (int)arg);
                lines.add(new Line(ip, prefix + text));
                return ip + 2;
            }
            case JIF, JIFKEEP, JITKEEP, JUMP -> {
                // Pattern: opcode jumpOffset (forwards)
                char jump = chunk.code(ip + 1);
                var text = String.format(" %04d -> %d", (int)jump,
                    ip + 2 + jump);  // Add jump
                lines.add(new Line(ip, prefix + text));
                return ip + 2;
            }
            case LOOP -> {
                // Pattern: opcode jumpOffset (backwards)
                char jump = chunk.code(ip + 1);
                var text = String.format(" %04d -> %d", (int)jump,
                    ip + 2 - jump); // Subtract jump
                lines.add(new Line(ip, prefix + text));
                return ip + 2;
            }
            case CONST, GLODEF, GLOGET, GLOSET -> {
                // Pattern: opcode constantIndex
                int index = chunk.code(ip + 1);
                var constant = joe.stringify(chunk.getConstant(index));
                if (constant.length() > 10) {
                    constant = constant.substring(0, 7) + "...";
                }
                var text = String.format(" %04d '%s'", index, constant);
                lines.add(new Line(ip, prefix + text));
                return ip + 2;
            }
            case ADD, DIV, EQ, FALSE, GE, GT, LE, LT, MUL,
                NE, NEGATE, NOT, NULL, POP, PRINT, RETURN, SUB, TRUE, UPCLOSE
            -> {
                // Pattern: opcode
                lines.add(new Line(ip, prefix));
                return ip + 1;
            }
            case CLOSURE -> {
                // Pattern: CLOSURE index [,isLocal, index]...
                var start = ip++;
                int constIndex = chunk.code(ip++);
                var func = (Function)chunk.getConstant(constIndex);
                var text = String.format(" %04d <fn %s>", constIndex, func.name());

                lines.add(new Line(start, prefix + text));

                for (var i = 0; i < func.upvalueCount; i++) {
                    int isLocal = chunk.code(ip);
                    int index = chunk.code(ip + 1);
                    var upText = String.format("   |  @%04d         %s %d",
                        ip, isLocal == 1 ? "local" : "upvalue", index);
                    lines.add(new Line(ip, upText));
                    ip += 2;
                }

                return ip;
            }
            default -> {
                // Pattern: opcode (unknown, presume no argument)
                var text = String.format(" %03d", (int)opcode);
                lines.add(new Line(ip, prefix + text));
                return ip + 1;
            }
        }
    }

    private String singlePrefix(int ip) {
        char opcode = chunk.code(ip);
        return String.format("%04d @%04d %-7s",
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

        return String.format("%s @%04d %-7s", line, ip, Opcode.name(opcode));
    }

    private boolean startsNewLine(int ip) {
        return ip == 0 || chunk.line(ip) != chunk.line(ip - 1);
    }

    //-------------------------------------------------------------------------
    // Helpers

    // A line of disassembly output, tied to its index in the code.
    private record Line(int ip, String text) {}
}
