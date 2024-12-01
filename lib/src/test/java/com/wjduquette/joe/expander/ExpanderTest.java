package com.wjduquette.joe.expander;

import com.wjduquette.joe.Args;
import com.wjduquette.joe.Joe;
import com.wjduquette.joe.Ted;
import org.junit.Before;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.*;

public class ExpanderTest extends Ted {
    Joe joe;
    Expander expander;

    @Before
    public void setup() {
        this.joe = new Joe();
        this.expander = new Expander(joe);
    }

    @Test
    public void testExpander() {
        test("testExpander");

        expander.expand("Foo <<bar>> baz.");
    }

}
