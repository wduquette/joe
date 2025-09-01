package com.wjduquette.joe.walker;

import com.wjduquette.joe.Environment;
import com.wjduquette.joe.RuntimeError;
import com.wjduquette.joe.scanner.Token;

/**
 * A Walker environment: a Joe engine environment extended to support
 * Walker's local scopes.  This class is intentionally package-private.
 */
class WalkerEnvironment extends Environment {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The enclosing scope, or null for the global scope.
    final WalkerEnvironment enclosing;

    //-------------------------------------------------------------------------
    // Constructors

    /**
     * Creates a global or package scope: an environment with no enclosing
     * environment.
     */
    WalkerEnvironment() {
        enclosing = null;
    }

    /**
     * Creates a local environment with the given enclosing environment.
     * @param enclosing The enclosing environment.
     */
    WalkerEnvironment(WalkerEnvironment enclosing) {
        this.enclosing = enclosing;
    }

    //-------------------------------------------------------------------------
    // WalkerEnvironment API

    /**
     * Get the variable named by the token, walking up the chain of
     * environments until it is found.
     * @param name The variable name token
     * @return The value
     * @throws RuntimeError if the variable is not found.
     */
    Object get(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name.span(),
            "Undefined variable: '" + name.lexeme() + "'.");
    }

    /**
     * Assigns a value to the variable, walking up the chain of
     * environments until it finds it.
     * @param name The variable name token
     * @param value The value
     * @throws RuntimeError if the variable is not found.
     */
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

    /**
     * Gets the value for the variable, looking in the environment
     * *distance* steps up the chain.  The `Resolver` computes this distance
     * to speed up local variable access.
     * @param distance The distance
     * @param name The variable name token
     * @return The value
     */
    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Assigns the value for the variable, looking in the environment
     * *distance* steps up the chain.  The `Resolver` computes this distance
     * to speed up local variable access.
     * @param distance The distance
     * @param name The variable name token
     * @param value The value
     */
    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme(), value);
    }

    /**
     * Returns the environment *distance* steps up the chain.  The
     * `Resolver` computes this distance to speed up local variable access.
     * @param distance The distance
     * @return The environment
     */
    WalkerEnvironment ancestor(int distance) {
        WalkerEnvironment environment = this;
        for (int i = 0; i < distance; i++) {
            assert environment != null;
            environment = environment.enclosing;
        }

        return environment;
    }
}
