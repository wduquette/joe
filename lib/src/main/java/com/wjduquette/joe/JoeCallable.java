package com.wjduquette.joe;

import java.util.List;

public interface JoeCallable {
    Object call(Interpreter interpreter, List<Object> args);
}
