package com.wjduquette.joe.tools.doc;

import com.wjduquette.joe.Ted;
import org.junit.Test;

import static com.wjduquette.joe.checker.Checker.check;
import static com.wjduquette.joe.checker.Checker.checkList;

public class ArgSpecTest extends Ted {

    @Test
    public void testBug_leadingOrTrailingOptionalArg() {
        var spec = "[x]";
        check(ArgSpec.isValid(spec)).eq(true);
        checkList(ArgSpec.names(spec)).items("x");
        check(ArgSpec.asMarkdown(spec)).eq("\\[*x*\\]");
    }

    @Test
    public void testNames() {
        test("testNames");
        checkList(ArgSpec.names("")).items();
        checkList(ArgSpec.names("x, y"))
            .items("x", "y");
        checkList(ArgSpec.names("x, y..."))
            .items("x", "y");
        checkList(ArgSpec.names("x, [y], z..."))
            .items("x", "y", "z");
    }

    @Test
    public void testIsValid() {
        test("testIsValid");
        check(ArgSpec.isValid("")).eq(true);
        check(ArgSpec.isValid("x, y")).eq(true);
        check(ArgSpec.isValid("x, y...")).eq(true);
        check(ArgSpec.isValid("x, [y], z...")).eq(true);

        check(ArgSpec.isValid("x, (y), z...")).eq(false);
    }

    @Test
    public void testAsMarkdown() {
        test("testAsMarkdown");
        check(ArgSpec.asMarkdown("")).eq("");
        check(ArgSpec.asMarkdown("x, y")).eq("*x*, *y*");
        check(ArgSpec.asMarkdown("x, y...")).eq("*x*, *y*...");
        check(ArgSpec.asMarkdown("x, [y], z...")).eq("*x*, \\[*y*\\], *z*...");
    }
}
