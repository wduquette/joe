package com.wjduquette.joe;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A function implemented in Joe.
 */
public class JoeFunction implements JoeCallable {
    private final Stmt.Function declaration;
    private final String signature;

    JoeFunction(Stmt.Function declaration) {
        this.declaration = declaration;

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

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Joe.exactArity(args, declaration.params().size(), signature);

        Environment environment = new Environment(interpreter.globals);

        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(),
                args.get(i));
        }

        interpreter.executeBlock(declaration.body(), environment);
        return null;
    }

    @Override
    public String toString() {
        return "<" + declaration.kind() + " " + declaration.name().lexeme() + ">";
    }
}
