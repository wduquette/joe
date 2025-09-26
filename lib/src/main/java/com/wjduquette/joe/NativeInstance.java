package com.wjduquette.joe;

import java.util.Map;

/**
 * A NativeInstance is an interface that provides the minimum features needed
 * to wrap a value as an {@link Instance}.  Native types can be made extensible
 * by a scripted classes if (A) their {@link ProxyType} supports it and
 * (B) the binding defines a subclass of the native type that implements this
 * interface.  Joe's `asJoeValue` method does the rest of the work.
 */
public interface NativeInstance {
    /**
     * Gets the instance's Joe class
     * @return The Joe class
     */
    JoeClass getJoeClass();

    /**
     * Gets the instance's field map.
     * @return The field map.
     */
    Map<String,Object> getInstanceFieldMap();
}
