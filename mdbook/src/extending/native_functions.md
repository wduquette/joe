# Native Functions

A *native function* is a Java method that is exposed as a function at the
script level.  Every native function has the same Java signature:

```java
Object _myFunction(Joe joe, ArgQueue args) { ... }
```

- Every native function returns a Joe value, possibly `null`.
- Every native function receives two arguments: `joe`, a reference to the
  `Joe` interpreter, and `args`, any arguments passed to the function.

It is the function's task to do the following:

- [Arity Checking](#arity-checking): Ensure that it has received the correct 
  number of arguments.
- [Argument Conversion](#argument-conversion): Ensure that those arguments have 
  the desired Java data types.
- Do the required computation—which often involves making a simple
  call to some Java API.
- [Return the Result](#return-the-result): If any.

Joe provides the tools to make each of these steps simple and concise.

For example, a simple function to square a number would look like this:

```java
Object _square(Joe joe, ArgQueue args) {
    args.exactArity(1, "square(x)");       // Arity check
    var x = joe.toDouble(args.next());     // Argument conversion
    return x*x;                            // Computation
}
```

## Installing a Native Function

To install a native function, use the `Joe::installGlobalFunction` method:

```java
var joe = new Joe();

joe.install("square", this::_square);
```

## Arity Checking

A function's *arity* is the number of arguments it takes.  Joe provides four
methods for checking that a native function has received the correct number of
arguments.  There are several cases:

### Exact Arity

The `Args::exactArity` method checks that the function has received exactly a 
specific number of arguments, as shown in the `_square()` example above.  It
takes two arguments:

```java
args.exactArity(1, "square(x)");
```

- The number of arguments expected.
- A string representing the function's signature.

If *args* doesn't contain exactly the correct number of arguments, 
`exactArity()` will use `Args.arityFailure()` to throw a `JoeError`
with this message:

`    Wrong number of arguments, expected: square(x)`

### Minimum Arity and Arity Ranges

Similarly, the `Args::minArity` method checks that the *args* contains
at least a minimum number of arguments.  For example, 
[`Number.max()`](../library/type.joe.Number.md#static.max) requires
at least 1 argument but can take any number of arguments.

```java
args.minArity(1, "Number.max(number,...)");
```

And the `Args::arityRange` method checks that the number of arguments
false within a certain range.  For example, the 
[`String`](../library/type.joe.String.md) type's 
[`substring()`](../library/type.joe.String.md#method.substring) method takes
1 or 2 arguments:

```java
args.arityRange(1, 2, "substring(beginIndex, [endIndex])");
```

### More Complex Cases

In rare cases, it's simplest for the native function to access the 
`Args`'s size directly, and throw an `arityFailure` explicitly:

```java
if (args.size() > 7) {
    throw Args.arityFailure("myFunc(a, b, c, ...)");
}
```

## Argument Conversion

Before passing an `Object` to a Java method, it's necessary to make sure it
has the correct type, and to produce an appropriate error message if it does
not.  Joe provides a family of argument conversion methods for this purpose;
client-specific converters are easily implemented.  

For example,

```java
var x = joe.toDouble(args.next());
```

- `args.next()` pulls the next unprocessed argument from the *args* queue.
- `joe.toDouble()` verifies that it's a `Double` and returns it as a `double`,
  or throws a `JoeError`.

The `JoeError` message will look like this:

`    Expected number, got: <actualType> '<actualValue>'.`

See the `Joe` class Javadoc for the family of converters, and the `Joe` class
source code for how they are implemented, and the many `TypeProxies` in
`com.wjduquette.joe.types` for examples of their use.

### Converting Strings

There are two ways to convert an argument that is to be treated as a String.

First, `joe.toString(arg)` will require that the value is actually a Java
`String`.

Alternatively, `joe.stringify(arg)` will convert the argument, of whatever type,
to its string representation.

Which of these you use will depend on the native function in question.  To
some extent it's a matter of taste.

### Converting Booleans

In Joe, any value can be used in a Boolean expression.  `false` and `null`
are interpreted as false; any other value is interpreted as true.  As a result,
it's best to convert arguments meant to be used as Booleans with the
`Joe.isTruthy()` function; this guarantees that the native function handles
boolean values in the same way as Joe does at the script level:

```java
var flag = Joe.isTruthy(arg);
```

## Returning the Result

If the function is called for its side effects and has no real result, it
must return `null`.

Any other value can be returned as is, but the following rules will make
life easier:

**Returning Integers**: cast integer results to `double`.  Joe understands 
doubles, but integers are an [opaque type](java_types.md#opaque-types).

```java
return (double)text.indexOf("abc");  // Convert integer index to double
```

**Returning Lists**: Most lists should be returned as `ListValue` values,
which can be modified freely at the script level.  

If java code produces a list that need not be modified, then it can 
be made read-only:

```java
return joe.readonlyList(myList);
```

If the list is not a `List<Object>`, and needs to be modified in place
at the script level, wrap it and specify the item type.  (This is
often necessary in JavaFX-related code):

```dtd
return joe.wrapList(myList, MyItemType.class);
```

Joe will then ensure that only values assignable to `MyItemType` are 
added to the list.

**Other Collections**: At time of writing, Joe does not yet support
maps or sets; when it does, they should be returned in the same way
as lists.

