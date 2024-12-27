package com.wjduquette.joe.bert;

/**
 * A scripted method bound to its instance
 */
public record BoundMethod(Object receiver, Closure method) {
}
