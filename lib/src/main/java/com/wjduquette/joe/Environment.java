package com.wjduquette.joe;

import java.util.*;

public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    void dump() {
        System.out.println(this);
        var map = new TreeMap<>(values);
        for (var key : map.keySet()) {
            System.out.printf("  %-20s %s\n", key, map.get(key).toString());
        }
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public List<String> getVarNames() {
        return new ArrayList<>(values.keySet());
    }

    public Object getVar(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (enclosing != null) return enclosing.getVar(name);

        return null;
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme() + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme() + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme(), value);
    }

    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            assert environment != null;
            environment = environment.enclosing;
        }

        return environment;
    }
}
