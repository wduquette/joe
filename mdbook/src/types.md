# Types and Values

In theory, every Java value, without exception, can be a valid Joe
value.  In practice, Joe provides particular support for the following
types:

- `null`
- [Booleans](#booleans)
- [Numbers](#numbers)
- [Strings](#strings)
- [Keywords](#keywords)
- [Pairs](#pairs)
- [Lists](#lists)
- [Errors](#errors)
- [Functions and Methods](#functions-and-methods)
- [Classes and Instances](#classes-and-instances)

## Booleans

[Boolean values](library/type.joe.Boolean.md) are represented by the 
familiar constants `true` and `false`, and internally as Java `Booleans`.

In boolean expressions, `false` and `null` count as false; all 
other values are considered "truthy".

## Numbers

All [numbers](library/type.joe.Number.md) are represented as IEEE 
double precision values, i.e., Java `Doubles`, as in Javascript and Lua.

Syntactically, numeric literals are defined as in Java.[^numeric]

- `123`
- `123.456`

## Strings

[Joe strings](library/type.joe.String.md) are represented internally 
as Java `String` values, and provide much the same range of operations.

String literals are double-quoted, as in Java:

- `"abcd"`
 
String literals support the usual array of escapes, including
`\n`, `\\`, `\"`, etc.[^unicode]

TODO: Include full list.

At time of writing, double-quoted strings can be multiline; no
attempt is made to clean up the whitespace.

## Keywords

A [Keyword](library/type.joe.Keyword.md) is an interned symbol, 
implemented internally using
Joe's `Keyword` class.  Keywords are frequently used where a Java 
program would use enumerations.

A keyword literal is an identifier preceded by a `#` character:

- `#yes`
- `#no`
- `#descending`

## Pairs

A [Pair](library/type.joe.Pair.md) is a pair of values, denoted `left`
and `right`; it's used to return a pair of values from a function.

```joe
var pair = Pair("abc", 123);
println("left  = " + pair.left());
println("right = " + pair.right());
```

## List

A [List](library/type.joe.List.md) is a Java `List<Object>` that 
contains an ordered collection of arbitrary Joe values, and has much
the same operations as Java lists.  See the link for the full API.

```joe
var list = List("a", "b", "c", "d");
println(list.get(2)); // Outputs "c".
```

## Errors

An [Error](library/type.joe.Error.md) is a Java `JoeError` exception:
a Joe runtime error, an error thrown explicitly by a Joe script, or
a Joe assertion failure.

Errors are thrown using the `throw` or `assert` statements, and can
be caught using the [catch()](library/pkg.joe.md#function.catch) 
function.

## Functions and Methods

Joe's functions and methods are first class values.  They can be
assigned to variables, passed to functions, and invoked at a later
time.  See the sections on [Functions](functions.md) and
[Classes](classes.md) for more information.

## Classes and Instances

Joe scripts can define classes; a class can have:

- An initializer
- Instance variables
- Instance methods
- Static variables
- Static methods

Joe classes support single inheritance.

See the section on [Classes](classes.md) for more information.

[^numeric]: At time of writing, Joe does not yet support exponential
notation or hexadecimal literals.

[^unicode]: Joe supports Unicode escapes, e.g., `\u1234`, as in Java;
but only within string literals.