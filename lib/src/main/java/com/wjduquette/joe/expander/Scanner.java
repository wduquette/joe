package com.wjduquette.joe.expander;

import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import static com.wjduquette.joe.expander.TokenType.*;

import java.util.ArrayList;
import java.util.List;

public class Scanner {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Expander expander;
    private final SourceBuffer buff;
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;

    //-------------------------------------------------------------------------
    // Constructor

    public Scanner(Expander expander, String name, String source) {
        this.expander = expander;
        this.buff = new SourceBuffer(name, source);
        this.source = source;
    }

    //-------------------------------------------------------------------------
    // Public API

    List<Token> getTokens() throws JoeError {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, null));
        return tokens;
    }

    private void scanToken() {
        switch (previousType()) {
            case TEXT -> {
                current = start + expander.getTemplateStart().length();
                addToken(START);
            }
            case START -> {
                current = source.indexOf(expander.getTemplateEnd(), start);
                if (current != -1) {
                    addToken(MACRO);
                } else {
                    throw new JoeError("Unterminated macro at (" +
                        buff.index2position(start) + ") in source.");
                }
            }
            case MACRO -> {
                current = start + expander.getTemplateEnd().length();
                addToken(END);
            }
            case END -> {
                current = source.indexOf(expander.getTemplateStart(), start);
                if (current == -1) {
                    current = source.length();
                }
                addToken(TEXT);
            }
        }
    }

    private TokenType previousType() {
        if (!tokens.isEmpty()) {
            return tokens.getLast().type();
        } else {
            return END; // Expecting TEXT next
        }
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void addToken(TokenType type) {
        var span = buff.span(start, current);
        tokens.add(new Token(type, span));
    }
}
