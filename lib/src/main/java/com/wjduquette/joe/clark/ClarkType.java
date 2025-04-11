package com.wjduquette.joe.clark;

/**
 * This interface describes Bert's scripted types for the purpose of defining
 * new types in the VM.
 */
interface ClarkType {
    void addMethod(String name, Closure closure);
    void addStaticMethod(String name, Closure closure);
}
