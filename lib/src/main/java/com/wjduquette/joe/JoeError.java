package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;

public class JoeError extends RuntimeException {
    //-------------------------------------------------------------------------
    // Instance Variables

    // Source line number
    private final int line;

    // Script level "stack frames"
    private final List<String> frames = new ArrayList<>();

    //-------------------------------------------------------------------------
    // Constructor

    /**
     * Creates an error with no line number info
     * @param message The error message.
     */
    public JoeError(String message, String... frames) {
        this(-1, message, frames);
    }

    /**
     * Creates an error with line number info
     * @param line The line number
     * @param message The error message.
     */
    public JoeError(int line, String message, String... frames) {
        super(message);
        this.line = line;
        this.frames.addAll(List.of(frames));
    }

    /**
     * Gets the line number, or -1 if no line number information is
     * available
     * @return The line number
     */
    public int line() {
        return line;
    }

    /**
     * Use this to add stack frames to an existing error.
     * @return The list of stack frame strings.
     */
    @SuppressWarnings("unused")
    public List<String> getFrames() {
        return frames;
    }

    public String getJoeStackTrace() {
        return frames.isEmpty()
            ? getMessage()
            : getMessage() + "\n  " + String.join("\n  ", frames);
    }
}
