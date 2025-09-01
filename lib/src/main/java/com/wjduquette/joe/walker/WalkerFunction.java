package com.wjduquette.joe.walker;

import com.wjduquette.joe.*;
import com.wjduquette.joe.parser.FunctionType;
import com.wjduquette.joe.parser.Parser;
import com.wjduquette.joe.parser.Stmt;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.scanner.Token;
import com.wjduquette.joe.types.ListValue;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A function implemented in Joe.
 */
final class WalkerFunction implements NativeCallable {
    private final Interpreter interpreter;
    private final Stmt.Function declaration;
    private final WalkerEnvironment closure;
    private final boolean isInitializer;
    private final boolean isVarArgs;
    private final String signature;
    private final boolean isLambda;

    //-------------------------------------------------------------------------
    // Constructor

    WalkerFunction(
        Interpreter interpreter,
        Stmt.Function declaration,
        WalkerEnvironment closure,
        boolean isInitializer
    ) {
        this.interpreter = interpreter;
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
        this.isVarArgs = isVarArgs(declaration.params());
        this.isLambda = declaration.type() == FunctionType.LAMBDA;
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

    public SourceBuffer.Span span() {
        return declaration.name().span();
    }

    WalkerFunction bind(JoeValue instance) {
        WalkerEnvironment environment = new WalkerEnvironment(closure);
        environment.setVar("this", instance);
        return new WalkerFunction(interpreter, declaration, environment,
            isInitializer);
    }

    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public Object call(Joe joe, Args args) {
        // FIRST, check the arity
        var expected = isVarArgs
            ? declaration.params().size() - 1
            : declaration.params().size();
        if (isVarArgs) {
            args.minArity(expected, signature);
        } else {
            args.exactArity(expected, signature);
        }

        // NEXT, create the environment for the arguments.
        WalkerEnvironment environment = new WalkerEnvironment(closure);

        for (int i = 0; i < expected; i++) {
            environment.setVar(declaration.params().get(i).lexeme(),
                args.next());
        }

        if (isVarArgs) {
            var varArgs = new ListValue(args.remainderAsList());
            environment.setVar(Parser.ARGS, varArgs);
        }

        try {
            var result = interpreter.executeBlock(declaration.body(), environment);
            if (isInitializer) return closure.getAt(0, "this");
            return result;
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
    }

    @Override
    public String callableType() {
        return declaration.type().text();
    }

    @Override
    public String signature() {
        return signature;
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    //-------------------------------------------------------------------------
    // Object API

    @Override
    public String toString() {
        return "<" + declaration.type().text() + " " + signature + ">";
    }
}
