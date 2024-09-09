package com.wjduquette.joe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A lambda function implemented in Joe.
 */
public final class JoeLambda implements JoeCallable {
    private final Expr.Lambda lambda;
    private final Environment closure;
    private final boolean isVarArgs;
    private final String signature;

    //-------------------------------------------------------------------------
    // Constructor

    JoeLambda(Expr.Lambda lambda, Environment closure) {
        this.lambda = lambda;
        this.closure = closure;
        this.isVarArgs = isVarArgs(lambda.params());
        this.signature = signature();
    }

    private boolean isVarArgs(List<Token> params) {
            return !params.isEmpty()
                && params.getLast().lexeme().equals(Parser.ARGS);
    }

    private String signature() {
        var params = lambda.params().stream()
            .map(Token::lexeme)
            .collect(Collectors.joining(", "));
        return "\\" + params + " -> ...";
    }

    //-------------------------------------------------------------------------
    // Methods

    @Override
    public Object call(Joe joe, ArgQueue args) {
        // FIRST, check the arity
        var expected = isVarArgs
            ? lambda.params().size() - 1
            : lambda.params().size();
        if (isVarArgs) {
            Joe.minArity(args, expected, signature);
        } else {
            Joe.exactArity(args, expected, signature);
        }

        // NEXT, create the environment for the arguments.
        Environment environment = new Environment(closure);

        for (int i = 0; i < expected; i++) {
            environment.define(lambda.params().get(i).lexeme(),
                args.next());
        }

        if (isVarArgs) {
            var varArgs = new ArrayList<>(args.remainder());
            environment.define(Parser.ARGS, varArgs);
        }

        try {
            var result = joe.interp().executeBlock(lambda.body(), environment);
            return result;
        } catch (Return returnValue) {
            return returnValue.value;
        }
    }

    @Override
    public String toString() {
        return "<lambda " + signature + ">";
    }
}
