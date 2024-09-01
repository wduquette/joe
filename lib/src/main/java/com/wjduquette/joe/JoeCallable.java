package com.wjduquette.joe;

import java.util.List;

public interface JoeCallable {
    Object call(Joe joe, List<Object> args);
}
