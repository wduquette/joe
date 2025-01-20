package com.wjduquette.joe.bert;

/**
 * A scripted method bound to its instance
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
