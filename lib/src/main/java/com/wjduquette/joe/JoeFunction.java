package com.wjduquette.joe;

import java.util.ArrayList;
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
    private final boolean isVarArgs;

    JoeFunction(
        Stmt.Function declaration,
        Environment closure,
        boolean isInitializer
    ) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
        this.isVarArgs = !declaration.params().isEmpty()
            && declaration.params().getLast().lexeme().equals(Parser.ARGS);

        // FIRST, compute the signature string.
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
        // FIRST, check the arity
        var expected = isVarArgs
            ? declaration.params().size() - 1
            : declaration.params().size();
        if (isVarArgs) {
            Joe.minArity(args, expected, signature);
        } else {
            Joe.exactArity(args, expected, signature);
        }

        // NEXT, create the environment for the arguments.
        Environment environment = new Environment(closure);

        for (int i = 0; i < expected; i++) {
            environment.define(declaration.params().get(i).lexeme(),
                args.get(i));
        }

        if (isVarArgs) {
            var varArgs = new ArrayList<>(args.subList(expected, args.size()));
            environment.define(declaration.params().getLast().lexeme(),
                varArgs);
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
