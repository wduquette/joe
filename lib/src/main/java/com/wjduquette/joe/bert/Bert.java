package com.wjduquette.joe.bert;

// Temporary main program.  Later this will be `BertEngine`, and we will
// invoke it from the main app.
public class Bert {
    public static void main(String[] args) {
        System.out.println("Bert!");
        var chunk = new Chunk();
        chunk.write(Opcode.CONSTANT);
        chunk.write(chunk.addConstant("Howdy!"));
        chunk.write(Opcode.RETURN);

        var dis = new Disassembler();
        System.out.println(dis.disassemble("test chunk", chunk));
    }

    public static String stringify(Object value) {
        return value.toString();
    }
}
