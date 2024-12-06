package com.wjduquette.joe.bert;

// Temporary main program.  Later this will be `BertEngine`, and we will
// invoke it from the main app.
public class Bert {
    public static void main(String[] args) {
        System.out.println("Bert!");
        var chunk = new Chunk();
        chunk.write(Opcode.CONSTANT, 1);
        chunk.write(chunk.addConstant("Howdy!"), 1);
        chunk.write(Opcode.RETURN, 2);

        var dis = new Disassembler();
        System.out.println(dis.disassemble("test chunk", chunk));
    }

    public static String stringify(Object value) {
        return value.toString();
    }
}
