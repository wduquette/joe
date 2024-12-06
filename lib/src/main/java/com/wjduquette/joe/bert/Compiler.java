package com.wjduquette.joe.bert;

class Compiler {
    Compiler() {
        // Nothing to do
    }

    public void compile(String source) {
        var scanner = new Scanner("*script*", source);

        var line = -1;
        for (;;) {
            var token = scanner.scanToken();
            if (token.line() != line) {
                System.out.printf("%4d ", token.line());
                line = token.line();
            } else {
                System.out.print("   | ");
            }
            System.out.println(token.type() + " '" + token.lexeme() + "'");

            if (token.type() == TokenType.EOF) break;
        }
    }
}
