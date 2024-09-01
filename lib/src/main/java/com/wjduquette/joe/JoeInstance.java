package com.wjduquette.joe;

public class JoeInstance {
    private final JoeClass klass;

    JoeInstance(JoeClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return "<" + klass.name() + "@" + String.format("%x",hashCode()) + ">";
    }
}
