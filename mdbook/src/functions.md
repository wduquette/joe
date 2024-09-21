# Functions

Joe functions are declared using the `function` statement.

```joe
function square(x) {
    return x*x; 
}
```

Use the `return` statement to return early from a function, or to
return a value.

## Variable Length Argument Lists

To write a function that takes a variable length argument list, use
the `args` parameter.  `args` will accept zero or more arguments, which
it will present to the function as a Joe [List](library/type.joe.List.md).

```joe
function howdy(greeting, args) {
    foreach (var person : args) {
        println(greeting + ", " + person + "!");
    }
}

// Says hello to Joe, Bob, Dave, and Ted
howdy("Hello", "Joe", "Bob", "Dave", "Ted");
```

If used, `args` must be the last argument in the list.

## Function References

Joe functions are first-class values, and can be passed to functions,
assigned to variables, and then called.

```joe
function square(x) { return x*x; }

var myFunc = square;

println(myFunc(5)); // Prints "25".
```

## Nested Functions

Functions can be declared in any scope, not just at the global scope, and
are visible only in that scope.

```joe
function a() {
    function b() { println("In function b."); }
    b(); // Prints "In function b."
}
b(); // Throws an unknown variable error.
```

## Lambda Functions

Joe supports lambda functions, i.e., unnamed function values.  The
syntax for defining a lambda function is:

- `\ <params> -> <expression>`
- `\ <params> -> { <statements> }`

For example, the following are all equivalent:

```joe
// A normal function called "lambda"
function lambda(x) { return x*x; }

// A lambda that squares a value
var lambda = \x -> x*x;

// The same lambda, expressed as a block
var lambda2 = \x -> { return x*x; };
```

The advantage of lambdas is that they can be passed directly to functions
without having to define a name:

```joe
var list = List(1,2,3,4,5);

// Compute a list of the squares of the numbers in the original list.
var squares = list.map(\x -> x*x);
```