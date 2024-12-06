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

        var vm = new VirtualMachine();
        println("== Execution ==");
        vm.interpret(chunk);
    }

    // Stand in for BertEngine::isDebug
    public static boolean isDebug() {
        return true;
    }

    // Stand in for Joe::stringify
    public static String stringify(Object value) {
        return switch (value) {
            case null -> "null";
            default -> value.toString();
        };
    }

    // Stand in for Joe::println
    public static void println(String text) {
        System.out.println(text);
    }
}
