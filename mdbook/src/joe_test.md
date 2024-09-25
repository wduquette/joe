# joe test

The `joe test` tool is used to execute test scripts for Joe code and
native APIs. This section explains how to write a test script,
run tests, and interpret the results; and also how to extend Joe's
test runner to include client-specific Joe 
[packages](extending/packages.md).

- [Writing a Test Script](#writing-a-test-script)
- [Test Outcomes](#test-outcomes)
- [Test Scaffolding](#test-scaffolding)
- [Test Checkers](#test-checkers)
- [Running Test Scripts](#running-test-scripts)
- [Extending the Test Runner](#extending-the-test-runner)

## Writing a Test Script

A test script is simply a Joe script containing tests, to be executed by
the `joe test` tool.  A test is a no-argument function whose name
begins with the string "test":

```joe
function testSomething() {
    // Test Something
}
```

`joe test` loads the file and then executes each of the test functions
one at a time, keeping track of the results.  **Note**: the order in which
the functions will be executed is not the same as the order in which
they appear in the file.  Take care to make each test independent of 
the other tests in the file.

## Test Outcomes

For each test there are three possible outcomes:

| Result  | Meaning                              |
|---------|--------------------------------------|
| Success | The test ran without error           |
| Failure | The test threw an `AssertError`      |
| Error   | The test threw some other `JoeError` |

Here's a successful test:

```joe
function testAddition() {
    var x = 2 + 2;
    assert x == 4;
}
```

And here's a failure:

```joe
function testAddition() {
    var x = 2 + 2;
    assert x == 5;
}
```

And here's an error; `println()` only takes a single argument

```joe
function testAddition() {
    var x = 2 + 2;
    println("x=", x);  // <- invalid syntax!
    assert x == 4;
}
```

## Test Scaffolding

In addition to the test functions, a test script can also contain any number
of other functions and classes, used as "scaffolding" by the tests.

However, individual test scripts are totally independent; they are executed
in distinct interpreters, so that there is no possibility of cross-talk.

## Test Checkers

The example above uses the `assert` statement to check the result of 
computations.  This is a viable alternative; in addition, Joe's 
test runner defines a collection of functions and methods for testing 
results more simply and with better error messages.  See the 
[Joe Test Tool API](library/pkg.joe.test.md) for the details.

For example, the test above would more typically be written like this:

```joe
function testAddition() {
    var x = 2 + 2;
    check(x).eq(4);
}
```

## Running Test Scripts

To run one or more test scripts, just pass their file names to `joe test`:

```shell
$ cd joe/tests
$ joe test *.joe

Successes: 118
Failures:  0
Error:     0
Total:     118

ALL TESTS PASS
```

By default, Joe will only display more information for failed tests:

```shell
$ joe test *.joe
testEndsWith                   in file type.joe.String.joe
  FAILED: Expected true, got: Boolean 'false'.

Successes: 117
Failures:  1
Error:     0
Total:     118
```

In particular, any output printed by tests is hidden.  To see all test
output, use the `--verbose` option (aka `-v`).  The output will list
each test script, each test name in the file, and any output related
to each test.

```shell
$ joe test -v *.joe

Running: lang_class.joe

testCanSetFields               in file lang_class.joe
testStaticInitializer          in file lang_class.joe
testInit                       in file lang_class.joe
testStaticMethods              in file lang_class.joe
testCanCallMethods             in file lang_class.joe
testClassCapturesScope         in file lang_class.joe
...

Successes: 118
Failures:  0
Error:     0
Total:     118

ALL TESTS PASS
```

## Extending the Test Runner

As with `joe run` and `joe repl`, Joe conducts all tests in a vanilla Joe
interpreter; the only extensions are `joe test`'s 
[test API](library/pkg.joe.test.md).  A client project will usually wish to use 
the test runner for its own Joe bindings.

And as with `joe run`, Joe can't predict the context in which those bindings
will need to be tested; it might be an entire scriptable application.  Thus,
Joe expects that client will provide their own test tool.

As of Joe 0.2.0, the simplest way to do this is to copy the test tool and
modify the copy.  The test tool is defined by the Java class
`com.wjduquette.joe.tools.test.TestTool`.  It is a short, straightforward
file.

The relevant code is in the `runTest()` method:

```java
// NEXT, configure the engine.
var joe = new Joe();
joe.installPackage(TestPackage.PACKAGE);
```

At this point in `runTest`, install any required native functions, types,
or packages as described in [Extending Joe](extending/extending.md).

We hope to make this simpler in a later release.