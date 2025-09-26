package com.wjduquette.joe;

import java.util.function.Consumer;

/**
 * A {@link JoeCallbackWrapper} for the Consumer&gt;T&lt; functional interface.
 * @param <T> The consumed value type.
 */
@SuppressWarnings("unused")
public class JoeConsumer<T>
    extends JoeCallbackWrapper
    implements Consumer<T>
{
    /**
     * Creates an instance.
     * @param joe The Joe interpreter
     * @param callable The callable.
     */
    public JoeConsumer(Joe joe, Object callable) {
        super(joe, callable);
    }

    @Override
    public void accept(T value) { callCallable(value); }
}
