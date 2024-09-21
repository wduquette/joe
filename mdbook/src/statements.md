# Statements

The Joe language provides the following statements.

Statements are terminated by a semicolon or a block, as in Java.

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

## Return

The `return` statement is used to return from functions and methods.  As in 
Java, it takes an optional expression to return a value.

It's an error to invoke the `return` statement at the top-level of a script.

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

`for` loops are defined as in Java.

```joe
for (var i = 0; i < 10; i = i + 1) {
    ...
}
```

## Break and Continue

The `break` and `continue` statements break out of the enclosing loop or 
continue with the next iteration of the enclosing loop, just as in Java.

There is no support for labeled breaks or continues in order to jump through
nested loops.

## Function Declarations

Functions are declared with the `function` statement.  See
[Functions](functions.md) for more details.

## Class Declarations

Classes are declared with the `class` statement.  See
[Classes](classes.md) for more details.

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

