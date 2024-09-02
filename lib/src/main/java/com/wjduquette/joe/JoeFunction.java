package com.wjduquette.joe;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A function implemented in Joe.
 */
public class JoeFunction implements JoeCallable {
    private final Stmt.Function declaration;
    private final String signature;
    private final Environment closure;
    private final boolean isInitializer;

    JoeFunction(
        Stmt.Function declaration,
        Environment closure,
        boolean isInitializer
    ) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;

        var params = declaration.params().stream()
            .map(Token::lexeme)
            .collect(Collectors.joining(", "));
        this.signature = declaration.name().lexeme() + "(" + params + ")";
    }

    /**
     * Return the name of the function.
     * @return The name
     */
    @SuppressWarnings("unused")
    public String name() {
        return declaration.name().lexeme();
    }

    /**
     * Returns the "kind" of the function, e.g., "function", "method"
     * @return The kind
     */
    public String kind() {
        return declaration.kind();
    }

    JoeFunction bind(JoeInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new JoeFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Joe joe, List<Object> args) {
        Joe.exactArity(args, declaration.params().size(), signature);

        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(),
                args.get(i));
        }

        try {
            var result = joe.interp().executeBlock(declaration.body(), environment);
            if (isInitializer) return closure.getAt(0, "this");
            return result;
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

    }

    @Override
    public String toString() {
        return "<" + declaration.kind() + " " + declaration.name().lexeme() + ">";
    }
}
