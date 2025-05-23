package com.wjduquette.joe.walker;

import com.wjduquette.joe.RuntimeError;
import com.wjduquette.joe.scanner.Token;

import java.util.*;

/**
 * A Joe environment, a dictionary of variable names and values.
 */
class Environment {
    final Environment enclosing;
    protected final Map<String, Object> values = new HashMap<>();

    /**
     * Creates an environment with no enclosing environment.
     * This is only done by the {@link GlobalEnvironment}.
     */
    public Environment() {
        enclosing = null;
    }

    /**
     * Creates an environment with the given enclosing environment.
     * @param enclosing The enclosing environment.
     */
    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }


    void dump() {
        System.out.println(this);
        var map = new TreeMap<>(values);
        for (var key : map.keySet()) {
            System.out.printf("  %-20s %s\n", key, map.get(key).toString());
        }
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name.span(),
            "Undefined variable: '" + name.lexeme() + "'.");
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

        throw new RuntimeError(name.span(),
            "Undefined variable '" + name.lexeme() + "'.");
    }

    public void setVar(String name, Object value) {
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
