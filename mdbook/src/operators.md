# Operators

Joe defines a subset of Java's operators, with a few differences.

- [Arithmetic Operators](#arithmetic-operators)
- [Comparison Operators](#comparison-operators)
- [Membership Operators](#membership-operators)
- [Matching Operator](#matching-operator)
- [Logical Operators](#logical-operators)
- [The Ternary Operator](#the-ternary-operator)
- [Assignment Operators](#assignment-operators)
- [Increment/Decrement Operators](#incrementdecrement-operators)
- [Property Reference Operator](#property-reference-operator)

## Arithmetic Operators

The `+`, `-`, `*`, and `/` operators (including unary `-`) are defined as in 
Java, with the usual precedence.

The `+` operator provides string concatenation instead of numeric addition
if at least one operand is a `String`.

## Comparison Operators

The `==` and `!=` compare for equality using the logic of Java's 
`Objects.equals()`; thus, they can be used to compare any two Joe values for 
equality.  As a result, Joe values do not provide an `equals()` method.

The `>`, `<`, `>=`, and `<=` operators compare numbers as they do in Java,
but also compare `Strings` lexicographically.

## Membership Operators

The `in` and `ni` ("Not In") operators check for membership of elements in 
collections, e.g., Joe [`List`](library/type.joe.List.md) values.

```joe
var list = List("A", "B", "C");

if ("A" in list) println("Got it!");
if ("D" ni list) println("Nope, not there!");
```

These operators work with any Java `Collection<?>`, and with any 
[registered type](extending/registered_types.md) that supports iteration.

## Matching Operator

The `~` operator matches a value against a 
[destructuring pattern](patterns.md), returning true or false.  For example,
here it used to determine whether `myValue` contains a three-item list whose
first item is `#fred`:

```joe
if (myValue ~ [#fred, _, _]) {
    println("Match!");
}
```

If the pattern contains binding variables, the variables are implicitly
declared within the current scope.  On a successful match they are set
to the matching values within the target; on failure they are set to
`null`:

```joe
if (myValue ~ [#fred, height, weight]) {
    println("Fred is " + height + " inches tall,");
    println("and weighs " + weight + " pounds.");
}
```

See the [Pattern Matching](patterns.md) section for more about pattern
matching and Joe's pattern syntax.

## Logical Operators

The `&&` and `||` operators provide short-circuit execution of Boolean
expressions in the usual way.  However, in Joe the values `false` and `null` 
are considered false in Boolean expressions, and all other values are 
considered true.  

Thus, instead of yielding `true` or `false`, `&&` and `||` yield the 
last operand to be evaluated.  Consider:

```joe
var x = 5;
var y = null;

println(x || 1); // Prints 5
println(y || 1); // Prints 1
```

The `!` operator negates a Boolean expression, yielding `true` or `false`.

## The Ternary Operator

The ternary (`? :`) operator is defined as in Java.

```joe
var x = 100;
println(x > 50 ? "big" : "small"); // Prints "big"
```

## Assignment Operators

The `=`, `+=`, `-=`, `*=`, and `/=` operators work essentially as they do in 
Java.

In addition, the `+=` operator concatenates strings if either the left or 
right-hand side is a string, just as `+` does.

```joe
x = y = 5;  // Assigns 5 to x and y.
```

## Increment/Decrement Operators

The `++` and `--` operators work as they do in Java.

## Property Reference Operator

The `@` operator can be used as a synonym for `this.` in class and record
instance methods, resulting in neater, more concise code. See
[Classes](classes.md) for details.
