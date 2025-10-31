# 'joe test'

The `joe test` tool is used to execute test scripts for Joe code and
native APIs. This section explains how to write a test script,
run tests, and interpret the results; and also how to extend Joe's
test runner to include client-specific Joe 
[packages](#testing-client-packages).

- [Options](#options)
- [Writing a Test Script](#writing-a-test-script)
- [Test Outcomes](#test-outcomes)
- [Test Scaffolding](#test-scaffolding)
- [Test Checkers](#test-checkers)
- [Running Test Scripts](#running-test-scripts)
- [Testing Client Packages](#testing-client-packages)

## Options

`joe test` takes the following options:

**--verbose**, **-v**

Enables verbose output.

**--libpath *path***, **-l *path***

Sets the [library path](#testing-client-packages) to the given path.  Use this
to test external packages.

**--clark**, **-c**

Use the "Clark" byte-engine (the default).

**--walker**, **-w**

Use the "Walker" AST-walker engine.

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

For each test there are four possible outcomes:

| Result  | Meaning                              |
|---------|--------------------------------------|
| Success | The test ran without error           |
| Skipped | The test was skipped                 |
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

## Running Test Scripts

To run one or more test scripts, just pass their file names to `joe test`:

```shell
$ cd joe/tests
$ joe test *.joe
Joe ?.?.? (clark engine)
Run-time: 0.152 seconds

Successes    413
Skipped        0
Failures       0
Errors         0
---------- -----
Total        413

ALL TESTS PASS
```

By default, Joe will only display more information for failed tests. 
In particular, any output printed by tests is hidden.  To see all test
output, use the `--verbose` option (aka `-v`).  The output will list
each test script, each test name in the file, and any output related
to each test.

## Testing Client Packages

`joe test` conducts all tests in a vanilla Joe that provides
interpreter; the only extensions are `joe test`'s 
[test API](library/pkg.joe.test.md).  Other packages provided as part of Joe can sometimes
be imported into test scripts using the `import` statement.

A client project's packages can be made available for import in one of two
ways.  

First, the user can configure `joe test` to load the required packages
from the client's `.jar` file by providing a `repository.nero` file
and using the `--libpath`.  See [Joe Package Repositories](package_repos.md)
for more information.

This approach might not work for client packages that require significant
access to a client's application's internals.  In particular, it will not
work for `NativePackages` that lack a no-argument constructor.

Second, the client project can implement its own test runner as a 
copy of the Joe `TestTool`. At present this requires copying the entire 
`TestTool` class, modifying it as needed, and executing it from a client 
project application.  This is not ideal, and in the future I intend to provide 
a better framework for this.
