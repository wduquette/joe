package com.wjduquette.joe.wrappers;

import com.wjduquette.joe.Joe;

import java.util.function.Consumer;

/**
 * A {@link CallbackWrapper} for the Consumer&gt;T&lt; functional interface.
 * @param <T> The consumed value type.
 */
@SuppressWarnings("unused")
public class ConsumerWrapper<T>
    extends CallbackWrapper
    implements Consumer<T>
{
    /**
     * Creates an instance.
     * @param joe The Joe interpreter
     * @param callable The callable.
     */
    public ConsumerWrapper(Joe joe, Object callable) {
        super(joe, callable);
    }

    @Override
    public void accept(T value) { callCallable(value); }
}
