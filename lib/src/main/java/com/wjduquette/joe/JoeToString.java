package com.wjduquette.joe;

/**
 * For types without TypeProxies that need a Joe in order to
 * produce a `toString()`.
 */
public interface JoeToString {
    String toString(Joe joe);
}
