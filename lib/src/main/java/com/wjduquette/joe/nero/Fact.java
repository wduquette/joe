package com.wjduquette.joe.nero;

import java.util.List;

/**
 * The Fact interface defines what {@link Nero} needs to know about
 * Fact objects in order to match {@link Rule rules} against them.
 */
public interface Fact {
    String relation();
    List<Object> terms();
}
