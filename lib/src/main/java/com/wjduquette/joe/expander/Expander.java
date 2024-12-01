package com.wjduquette.joe.expander;

import com.wjduquette.joe.Joe;

public class Expander {
    //-------------------------------------------------------------------------
    // Instance Variables

    private final Joe joe;
    private String templateStart = "<<";
    private String templateEnd = ">>";

    //-------------------------------------------------------------------------
    // Constructor

    public Expander(Joe joe) {
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

    public String expand(String source) {
        var tokens = new Scanner(this, "*expand*", source).getTokens();
        for (var token : tokens) {
            System.out.println(token);
        }

        return null;
    }
}
