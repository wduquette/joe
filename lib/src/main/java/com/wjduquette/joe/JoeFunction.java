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

    JoeFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;

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
        return new JoeFunction(declaration, environment);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Joe.exactArity(args, declaration.params().size(), signature);

        Environment environment = new Environment(closure);

        for (int i = 0; i < declaration.params().size(); i++) {
            environment.define(declaration.params().get(i).lexeme(),
                args.get(i));
        }

        try {
            return interpreter.executeBlock(declaration.body(), environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
    }

    @Override
    public String toString() {
        return "<" + declaration.kind() + " " + declaration.name().lexeme() + ">";
    }
}
