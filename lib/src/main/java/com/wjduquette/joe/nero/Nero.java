package com.wjduquette.joe.nero;

import com.wjduquette.joe.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Nero is the public entry point for parsing and executing Nero
 * code.
 */
public class Nero {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private boolean gotError = false;

    //-------------------------------------------------------------------------
    // Constructor

    public Nero(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Public API

    /**
     * Just a convenient entry point for getting some source code into
     * the module.  This will undoubtedly change a lot over time.
     *
     * @param buff The Nero source.
     * @throws JoeError if the script could not be compiled.
     */
    public void parse(SourceBuffer buff) {
        gotError = false;

        var scanner = new Scanner(buff, this::errorHandler);
        var tokens = scanner.scanTokens();
        if (gotError) throw new JoeError("Error in Nero input.");

        tokens.forEach(System.out::println);
    }

    /**
     * Processes the given file in some way.
     * @param scriptPath The file's path
     * @throws IOException if the file cannot be read.
     * @throws JoeError if the script could not be compiled.
     */
    public void parseFile(String scriptPath)
        throws IOException, SyntaxError
    {
        var path = Paths.get(scriptPath);
        byte[] bytes = Files.readAllBytes(path);
        var script = new String(bytes, Charset.defaultCharset());

        parse(new SourceBuffer(path.getFileName().toString(), script));
    }

    private void errorHandler(Trace trace) {
        gotError = true;
        System.out.println("line " + trace.line() + ":" +
            trace.message());
    }
}
