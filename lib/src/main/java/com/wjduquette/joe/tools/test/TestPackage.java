package com.wjduquette.joe.tools.test;

import com.wjduquette.joe.*;

class TestPackage extends JoePackage {
    public static final JoePackage PACKAGE = new TestPackage();

    public TestPackage() {
        super("joe.test");
        globalFunction("assertEquals", this::_assertEquals);
        globalFunction("fail",         this::_fail);
        scriptResource(getClass(), "pkg.joe.test.joe");
    }

    private Object _assertEquals(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "assertEquals(got, expected)");
        var got = args.getRemaining(0);
        var expected = args.getRemaining(1);

        if (!Joe.isEqual(got, expected)) {
            throw new AssertError("Expected '" +
                joe.stringify(expected) + "', got: '" +
                joe.stringify(got) + "'.");
        }

        return null;
    }

    private Object _fail(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 1, "fail(message)");
        throw new AssertError(joe.stringify(args.getRemaining(0)));
    }
}
