package com.wjduquette.joe;

import java.io.IOException;

public class App {

    //-------------------------------------------------------------------------
    // Main

    public static void main(String[] args) throws IOException {
        var joe = new Joe();

        if (args.length > 1) {
            System.out.println("Usage: joe [script]");
            System.exit(64);
        } else if (args.length == 1) {
            joe.runFile(args[0]);
        } else {
            joe.runPrompt();
        }
    }
}
