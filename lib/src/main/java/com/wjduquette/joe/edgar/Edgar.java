package com.wjduquette.joe.edgar;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;

public class Edgar {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private String templateStart = "<<";
    private String templateEnd = ">>";

    //-------------------------------------------------------------------------
    // Constructor

    public Edgar(Joe joe) {
        this.joe = joe;
    }

    //-------------------------------------------------------------------------
    // Configuration

    public String getTemplateStart() {
        return templateStart;
    }

    public void setTemplateStart(String templateStart) {
        this.templateStart = templateStart;
    }

    public String getTemplateEnd() {
        return templateEnd;
    }

    public void setTemplateEnd(String templateEnd) {
        this.templateEnd = templateEnd;
    }

    //-------------------------------------------------------------------------
    // Expansion

    public String expand(String source) throws JoeError {
        var tokens = new Scanner(this, "*expand*", source).getTokens();
        var buff = new StringBuilder();

        for (var token : tokens) {
            switch (token.type()) {
                case TEXT -> buff.append(token.text());
                case MACRO -> {
                    try {
                        var result = joe.run("*expand*", token.text() + ";");
                        buff.append(joe.stringify(result));
                    } catch (JoeError ex) {
                        throw ex.addInfo("At (" +
                            token.span().startPosition() + ") in source");
                    }
                }
                default -> {} // Do nothing
            }
        }

        return buff.toString();
    }
}
