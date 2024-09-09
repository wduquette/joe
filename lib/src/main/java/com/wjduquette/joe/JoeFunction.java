package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A function implemented in Joe.
 */
public final class JoeFunction implements JoeCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;
    private final boolean isVarArgs;
    private final String signature;
    private final boolean isLambda;

    //-------------------------------------------------------------------------
    // Constructor

    JoeFunction(
        Stmt.Function declaration,
        Environment closure,
        boolean isInitializer
    ) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
        this.isVarArgs = isVarArgs(declaration.params());
        this.isLambda = declaration.kind().equals("lambda");
        this.signature = makeSignature();
    }

    private boolean isVarArgs(List<Token> params) {
            return !params.isEmpty()
                && params.getLast().lexeme().equals(Parser.ARGS);
    }

    private String makeSignature() {
        var params = declaration.params().stream()
            .map(Token::lexeme)
            .collect(Collectors.joining(", "));
        if (isLambda) {
            return declaration.name().lexeme() + params;
        } else {
            return declaration.name().lexeme() + "(" + params + ")";
        }
    }

    //-------------------------------------------------------------------------
    // Methods

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
    public Object call(Joe joe, ArgQueue args) {
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
                args.next());
        }

        if (isVarArgs) {
            var varArgs = new ArrayList<>(args.remainder());
            environment.define(Parser.ARGS, varArgs);
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
        return "<" + declaration.kind() + " " + signature + ">";
    }
}
