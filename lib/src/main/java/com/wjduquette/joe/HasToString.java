package com.wjduquette.joe;

/**
 * For types without TypeProxies that need `Joe` in order to
 * produce a script-level `toString()`.
 */
public interface HasToString {
    String toString(Joe joe);
}
