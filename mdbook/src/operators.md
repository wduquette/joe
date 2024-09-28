# Operators

Joe defines a subset of Java's operators, with a few differences.

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

These operators work with the same set of collection values as the 
[`foreach` statement](statements.md#foreach-loops).

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

The `=`, `+=`, `-=`, `*=`, and `/=` operators work as they do in Java.

In particular:

- The `+=` operator can concatenate strings if the variable
  or property on the left-hand side contains a string; the right operand
  will be converted to a string.

- An assignment expression yields the assigned value.

```joe
x = y = 5;  // Assigns 5 to x and y.
```

## Pre- and Post-Increment/Decrement Operators

The `++` and `--` operators work as they do in Java.