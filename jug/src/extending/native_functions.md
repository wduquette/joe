# Native Functions

A *native function* is a Java method that is exposed as a function at the
script level.  Every native function has the same Java signature:

```java
Object _myFunction(Joe joe, Args args) { ... }
```

- Every native function returns a Joe value, possibly `null`.
- Every native function receives two arguments: `joe`, a reference to the
  `Joe` interpreter, and `args`, any arguments passed to the function.

It is the function's task to do the following:

- [Arity Checking](#arity-checking): Ensure that it has received the correct 
  number of arguments.
- [Argument Conversion](#argument-conversion): Ensure that those arguments have 
  the desired Java data types, and throw meaningful errors if they do not.
- Do the required computation—which often involves making a simple
  call to some Java API.
- [Return the Result](#return-the-result): If any.

Joe provides the tools to make each of these steps simple and concise.

For example, a simple function to square a number would look like this:

```java
Object _square(Joe joe, Args args) {
    args.exactArity(1, "square(x)");       // Arity check
    var x = joe.toDouble(args.next());     // Argument conversion
    return x*x;                            // Computation and return
}
```

## Installing a Native Function

To install a native function into a Joe interpreter, use the 
`Joe::installGlobalFunction` method:

```java
var joe = new Joe();
...
joe.installGlobalFunction("square", this::_square);
```

## Arity Checking

A function's *arity* is the number of arguments it takes.  Joe provides four
methods for checking that a native function has received the correct number of
arguments.  There are several cases:

**Exact Arity:** the `Args::exactArity` method checks that the function has 
received exactly the required number of arguments, as shown in the `_square()` 
example above.  It takes two arguments: the required number, and the 
function's signature:

```java
args.exactArity(1, "square(x)");
```

If *args* contains an incorrect number of arguments, 
`exactArity()` will throw a `JoeError`:

`Wrong number of arguments, expected: square(x).`

**Minimum Arity**: the `Args::minArity` method checks that the 
*args* contains at least a minimum number of arguments.  For example, 
[`Number.max()`](../library/type.joe.Number.md#static.max) requires at least 1 argument but can take any number of 
arguments.

```java
args.minArity(1, "Number.max(number,...)");
```

**Arity Range**: the `Args::arityRange` method checks that the number of 
arguments false within a certain range.  For example, the 
[`String`](../library/type.joe.String.md) type's 
[`substring()`](../library/type.joe.String.md#method.substring) method takes
1 or 2 arguments:

```java
args.arityRange(1, 2, "substring(beginIndex, [endIndex])");
```

**More Complex Cases**: in rare cases the function will examine the arguments
more closely and throw an `arityFailure` explicitly if some required pattern
isn't found.

```java
if (args.size() > 7) {
    throw Args.arityFailure("myFunc(a, b, c, ...)");
}
```

## Argument Conversion

Before passing an `Object` to a Java method it's necessary to cast it or 
convert it to the required type.  Joe catches unexpected Java errors in
native functions, but it's better to check explicitly and to produce an 
appropriate error message if a value has the wrong type. Joe provides a 
family of argument conversion methods for this purpose, and client-specific 
converters are easily implemented.  

For example,

```java
var x = joe.toDouble(args.next());
```

- `args.next()` pulls the next unprocessed argument from the *args* queue.
- `joe.toDouble()` verifies that it's a `Double` and returns it as a `double`,
  or throws a `JoeError` if it is not.

The `JoeError` message will look like this:

`Expected number, got: <actualType> '<actualValue>'.`

See:

- The `Joe` class's Javadoc for the family of converters
- The `Joe` class's source code for how they are implemented
- The many `ProxyTypes` in `com.wjduquette.joe.types` for examples of their use.

### Converting Strings

There are three ways to convert an argument that is to be treated as a String.

- `joe.toString(arg)` requires that the value is already a Java `String`.
- `joe.stringify(arg)` will convert the argument, of whatever type,
  to its Joe-specific string representation.  Proxy types can customize 
  that representation.
- `arg.toString()` will convert the argument to its default Java string
  representation.  Usually it's better to use `joe.stringify(arg)`.

Which of these you use will depend on the native function in question.  To
some extent it's a matter of taste.

### Converting Booleans

Joe allows any value to appear in Boolean expressions.  `false` and `null`
are interpreted as false; all other values are interpreted as true.  As a result,
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
supports doubles directly, but integers are an [opaque type](java_types.md#opaque-types).

```java
return (double)text.indexOf("abc");  // Convert integer index to double
```

**Returning Lists**: Most lists should be returned as `ListValue` values,
which can be modified freely at the script level.  

If Java code produces a list that need not or must not be modified, then it can 
be made read-only:

```java
return joe.readonlyList(myList);
```

If the Java list is not a `List<Object>`, and needs to be modified in place
at the script level, wrap it and specify the item type.  (This is
often necessary in JavaFX-related code):

```java
return joe.wrapList(myList, MyItemType.class);
```

Joe will then ensure that only values assignable to `MyItemType` are 
added to the list.  There are several variants of `Joe::wrapList`.

**Other Collections**: Joe supports maps and sets in much the same way as
lists.
