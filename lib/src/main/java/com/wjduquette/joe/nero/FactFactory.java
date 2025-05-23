package com.wjduquette.joe.nero;

import java.util.List;

/**
 * A functional interface for creating new facts.
 */
public interface FactFactory {
    Fact create(String relation, List<Object> fields);
}
