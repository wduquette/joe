package com.wjduquette.joe;

import java.util.Collection;

/**
 * Any object that can return a collection of items can be iterated over
 * using Joe's `foreach` statement.
 */
public interface JoeIterable {
    Collection<Object> getItems();
}
