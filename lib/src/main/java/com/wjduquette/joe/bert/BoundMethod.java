package com.wjduquette.joe.bert;

/**
 * A scripted method bound to its instance, producing a callable.
 *
 * <p>
 * The {@code method} {@link Closure} is a compiled function that expects
 * to find the receiver as the local variable in slot 0.  (Non-method
 * closures also have a local variable in slot 0, but they ignore it.)
 * </p>
 *
 * <p>
 * When the virtual machine executes a BoundMethod it pushes the receiver
 * onto the stack, and then handles the Closure like any other function.
 * </p>
 */
public record BoundMethod(Object receiver, Closure method)
    implements BertCallable
{
    //-------------------------------------------------------------------------
    // JoeCallable API

    @Override
    public String callableType() {
        return method.callableType();
    }

    @Override
    public boolean isScripted() {
        return true;
    }

    @Override
    public String signature() {
        return method.signature();
    }
}
