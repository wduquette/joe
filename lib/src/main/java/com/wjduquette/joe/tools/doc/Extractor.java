package com.wjduquette.joe.tools.doc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Extractor {
    private static final String START = "//**";
    private static final String BODY = "//";

    //-------------------------------------------------------------------------
    // Static Methods

    public static List<Line> process(Path file) {
        return new Extractor(file).extract();
    }

    //-------------------------------------------------------------------------
    // Instance Variables

    private final Path file;

    //-------------------------------------------------------------------------
    // Extractor

    private Extractor(Path file) {
        this.file = file;
    }

    //-------------------------------------------------------------------------
    // Methods

    private List<Line> extract() {
        List<String> lines;
        List<Line> result = new ArrayList<>();

        try (var stream = Files.lines(file)) {
            lines = stream.toList();
        } catch (IOException ex) {
            System.out.println("*** Failed to scan '" + file + "':\n" +
                ex.getMessage());
            return result;
        }

        var inBlock = false;
        var number = 0;

        for (var line : lines) {
            ++number;
            if (inBlock) {
                if (line.trim().startsWith(BODY + " ")) {
                    line = line.stripLeading().substring(3);
                    result.add(new Line(number, line));
                } else if (line.trim().equals(BODY)) {
                    result.add(new Line(number, ""));
                } else {
                    inBlock = false;
                }
            } else if (line.trim().startsWith(START)) {
                inBlock = true;
            }
        }

        return result;
    }
}
