package com.wjduquette.joe;

public interface JoeClass extends JoeCallable {
    String name();

    JoeFunction findMethod(String name);
}
