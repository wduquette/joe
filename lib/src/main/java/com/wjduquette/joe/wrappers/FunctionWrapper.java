package com.wjduquette.joe.wrappers;

import com.wjduquette.joe.Joe;

import java.util.function.Function;

/**
 * A {@link CallbackWrapper} for the Function&gt;T,Object&lt; functional
 * interface.
 * @param <T> The input value type.
 */
@SuppressWarnings("unused")
public class FunctionWrapper<T>
    extends CallbackWrapper
    implements Function<T,Object>
{
    /**
     * Creates an instance.
     * @param joe The Joe interpreter
     * @param callable The callable.
     */
    public FunctionWrapper(Joe joe, Object callable) {
        super(joe, callable);
    }

    @Override
    public Object apply(T value) {
        return callCallable(value);
    }
}
