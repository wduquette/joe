# Statements

The Joe language provides the following statements. Statements are terminated 
by a semicolon or (sometimes) a block, as in Java.

- [Variable Declarations](#variable-declarations)
- [Function Declarations](#function-declarations)
- [Class and Record Declarations](#class-declarations)
- [Expression Statements](#expression-statements)
- [Blocks](#blocks)
- [Return Statement](#return)
- [If Statements](#if-statements)
- [While Loops](#while-loops)
- [For Loops](#for-loops)
- [Foreach Loops](#foreach-loops)
- [Break and Continue](#break-and-continue)
- [Switch Statements](#switch-statements)
- [Match Statements](#match-statements)
- [Throw](#throw)
- [Assert](#assert)

## Variable Declarations

All variables must be declared before use using the `var` statement.
The `var` statement can assign an initial value; if the initial value
is omitted, the variable is initialized to `null`.

```joe
var x = 5;
var y;      // y == null
```

Joe is lexically scoped; undeclared variables in function or method bodies
are presumed to be declared in an enclosing scope.  It is an error if
they are not.

In addition to declaring and initializing individual variables, the
`var` statement can use a [pattern](patterns.md) to perform a destructuring 
bind: that is,
to bind one or more variables to values within a data structure.  For example,
suppose that the function `f()` returns a two-item list, and the caller wants
to assign the list items to the variables `x` and `y`.

One could do this:

```joe
var result = f();
var x = result[0];
var y = result[1];
```

Or, one could do a destructing bind:

```joe
var [x, y] = f();
```

Here, `var` matches the list returned by `f()` against the pattern
`[x, y]` and binds variables `x` and `y` to the matched values.

**When the pattern doesn't match:** If `var`'s pattern doesn't match
the target value, e.g., if `f()` didn't return a two-item list in the
example shown above, then the pattern match will fail and Joe will
throw a runtime error.  Therefore, `var` should only be used when the
shape of the target value is known ahead of time.  Use
the [`~` operator](operators.md#matching-operator) or the
[`match` statement](#the-match-statement) to test whether a value matches a
particular pattern.

See [Pattern Matching](patterns.md) for more on pattern matching and
destructuring binds, including Joe's full pattern syntax.

## Function Declarations

Functions are declared with the `function` statement.  See
[Functions](functions.md) for more details.

## Class and Record Declarations

[Class](classes.md) and [record](records.md) types are declared with 
the `class` and `record` statements respectively.

## Expression Statements

Any expression becomes a statement when followed by a semicolon.

```joe
x + y;
doSomething();
```

To execute an expression and see its value in `joe repl`, enter the 
expression as an expression statement.

## Blocks

A block is a sequence of statements enclosed in curly brackets, as in 
Java.  Each block defines a lexical scope.

```joe
var x = 5;
{
    var x = 6;
    println(x); // Prints "6"
}
println(x);     // Prints "5"
```

## Return Statement

The `return` statement is used to return from functions and methods.  As in 
Java, it takes an optional expression to return a value.

When used at script level, `return` terminates the script, optionally
returning a value.  This value is displayed in the REPL and is
accessible via Joe's [embedding](embedding/embedding.md) API, but is
not displayed by `joe run`. 

## If Statements

`if` statements are defined as in Java.

```joe
if (x == 5) {
    ...
} else if (x == 15) {
    ...
} else {
    ...
}
```

## While Loops

`while` loops are defined as in Java.

```joe
var x = 0;
while (x < 10) {
    ...
    x = x + 1;
}
```

## For Loops

`for` loops are defined as in Java, except that `for (var item : list)`
is not supported.  See [Foreach Loops](#foreach-loops), below.

```joe
for (var i = 0; i < 10; i = i + 1) {
    ...
}
```

## Foreach Loops

`foreach` loops allow iteration over the members of a collection, e.g.,
a Joe `List`.

```joe
var list = ["a", "b", "c"];

// Prints "a", "b", and "c" on successive lines.
foreach (item : list) {
    println(item);
} 
```

In addition, `foreach` can use a [pattern](patterns.md) to do a 
destructuring bind on each list item:

```joe
var list = [[#a, 1], [#b, 2], #whoops, [#c, 3]];

// Prints #a, #b, and #c on successive lines
foreach ([x, _] : list) {
    println(x);
}
```

`foreach` will silently ignore any list items that 
don't match the pattern, making it a useful tool for extracting data
from homogeneous lists.

See [Pattern Matching](patterns.md) for more on pattern matching and
destructuring binds, including Joe's full pattern syntax.

## Break and Continue

The `break` and `continue` statements break out of the enclosing loop or 
continue with the next iteration of the enclosing loop, just as in Java.

There is no support for labeled breaks or continues in order to jump through
nested loops.

## Switch Statements

Joe's `switch` statement uses Java's enhanced switch syntax rather than
the classic C-like syntax:

```joe
switch (x) {
    case 1, 2, 3 -> return "abc";
    case 4, 5, 6 -> return "def";
    case 7, 8, 9 -> return "ghi";
    default -> return "???";
}
```

- The switch value and the case values can be any Joe value *or
  expression*.
- There must be at least one `case` clause.
- Each `case` can have one or more values to match.
- Each case's body can be a single statement or a block.
- If present, the `default` clause must follow all the `case` clauses.

The implementation is quite simple: each case value is checked in 
turn, from the top to the bottom, until a match is found; then the
case's body is executed.

## Match Statements

The `match` statement is similar to a `switch` statement, but matches
patterns against a target value instead of checking for equality.  It
is especially useful for processing a heterogeneous list of values.

```joe
match (value) {
    case [a, b] -> 
        println("Two item list of " a + " and " + b + ".");
    case Person(name, age) -> 
        println("Person " + name + " is " + age + " years old.");
    default -> println("no match");
}
```

Every `match` statement requires at least one `case`; the `default`
case is optional.  (Note that matching on `_`, a simple wildcard pattern, 
is equivalent to the `default` case:

```joe
match (value) {
    case [a, b] -> 
        println("Two item list of " a + " and " + b + ".");
    case Person(name, age) -> 
        println("Person " + name + " is " + age + " years old.");
    case _ -> println("no match");
}
```

Each `case` in a `match` can include an optional guard clause that
adds a boolean guard condition on top of the pattern match.  In the following
example the case matches any `Person` record, and then requires that the
person be at least 10 years old.

```joe
match (value) {
    ...
    case Person(name, age) if age >= 10 -> 
        println("Person " + name + " is at least 10 years old.");
    ...
}
```

See [Pattern Matching](patterns.md) for more on pattern matching and
destructuring binds, including Joe's full pattern syntax.

## Throw

The `throw` statement is used to explicitly throw 
[error exceptions](library/type.joe.Error.md) in a Joe script.

```joe
if (x <= 0) {
    throw Error("Expected a positive number.");
}
```

For convenience the message string can be thrown directly; the following
code is equivalent to that above:

```joe
if (x <= 0) {
    throw "Expected a positive number.";
}
```

Thrown errors can be caught using the 
[catch()](library/pkg.joe.md#function.catch) function; and once caught
can be rethrown.

## Assert

The `assert` statement checks a condition and throws an error if it
is unmet.  Assertions are always checked; they are not disabled in
production as in Java.

```joe
assert x > 0;
```

The programmer may provide an optional message.

```joe
assert x > 0, "x must be positive!";
```

The various checkers and assertion functions defined in 
`joe test`'s [Test API](library/pkg.joe.test.md) all work in terms of
the `assert` statement.

