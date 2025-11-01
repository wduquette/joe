# Types and Values

Every Java `Object`, without exception, is a valid Joe value. However,
Joe provides particular support for the following types:

- `null`
- [Booleans](#booleans)
- [Numbers](#numbers)
- [Strings](#strings)
- [Raw String Literals](#raw-string-literals)
- [Keywords](#keywords)
- [Lists](#lists)
- [Sets](#sets)
- [Maps](#maps)
- [Errors](#errors)
- [Functions and Methods](#functions-and-methods)
- [Classes](#classes)
- [Records](#records)
- [Rule Sets](#rule-sets)

## `null`

It's fashionable to decry the inclusion of `null` in a language these
days, but `null` is a fact of life in Java, and Joe is meant to 
interoperate with Java.

## Booleans

[[joe.Boolean]] values are represented by the 
familiar constants `true` and `false`, and internally as Java `Booleans`.

In boolean expressions, `false` and `null` count as false; all 
other values are considered "truthy".

## Numbers

All [[joe.Number|numbers]] are represented as IEEE 
double precision values, i.e., Java `Doubles`, as in Javascript and Lua.

Syntactically, numeric literals are defined as in Java.

- `123`
- `123.456`
- `123.4e-56`
- `0x12FF`

## Strings

Joe [[joe.String|strings]] are represented internally 
as Java `String` values, and provide much the same range of operations.

String literals are double-quoted, as in Java:

- `"abcd"`
 
String literals support the usual array of escapes, including
`\n`, `\\`, `\"`, `\t`, `\b`, `\r`, `\f` and unicode escapes.[^unicode]

In addition, Joe supports multiline text blocks delimited by `"""`, 
somewhat like Java's text blocks:[^textblocks]

```joe
var block = """
    Line 1
      "Line 2"
           Line 3
    """;
```

Text blocks follow these rules:

- Leading blank lines are stripped.
- Trailing whitespace is stripped
- What remains is outdented to the left margin via Java's
  `String::stripIndent()` method, which preserves relative whitespace.
- Text blocks can contain all the usual escapes, including `\"`.
- But single `"` characters in a text block *need not* be escaped.

Thus, the block shown above would print like this:

```text
Line 1
  "Line 2"
      Line 3
```

## Raw String Literals

A string entered using single quotes is a raw string literal.  In a 
raw string literal the backslash is just another character; the literal 
`'\n'` yields a string containing a backslash followed by a lower-case `n`.

Similarly, a text block contained within `'''` delimiters is a raw text
block, and is processed in the same way: as a normal text block, but
without any backslash escapes.

The primary use for raw strings is to make regular expressions more
readable.

## Keywords

A [[joe.Keyword]] is a symbolic value, implemented internally using
Joe's `Keyword` class.  Keywords are frequently used where a Java 
program would use enumerations.

A keyword literal is an identifier preceded by a `#` character:

- `#yes`
- `#no`
- `#descending`

It is common for native API bindings that expect Java enums
to accept keyword values as well, provided that the keyword's name 
matches the enum's name (disregarding case).

## Lists

A [[joe.List]] is a Java `List<Object>` that contains an ordered collection 
of arbitrary Joe values, and has much the same operations as Java 
lists.  See the link for the full API.

There are several ways to create and initialize lists.  First,
build it up one element at a time:

```joe
var list = List();
list.add("a");
list.add("b");
list.add("c");
list.add("d");
```

Or use the Java-like `List.of(items...)` method:

```joe
var list = List.of("a", "b", "c", "d");
```

However, lists are usually created using Joe's list literal syntax:

```joe
var empty = [];
var list = ["a", "b", "c", "d"];
```

List literals are allowed to have a trailing comma:

```joe
var list = [
    "this",
    "that",
    "the other",
];
```

Lists can be queried much as in Java:

```joe
var list = ["a", "b", "c", "d"];

list.isEmpty();      // False
list.size();         // 4
list.get(1);         // "b"
```

List items can also be accessed using array notation:

```joe
var list = ["a", "b", "c", "d"];

var c = list[2];    // list.get(2)
list[3] = "xyz";    // list.set(3, "xyz");
```

Indices are zero-based, and must refer to an existing item.

## Sets

A [[joe.Set]] is a Java `Set<Object>`.  It has much
the same operations as Java sets, along with some advanced methods.

There are several ways to create sets:

```joe
var set = Set();
set.add(#a);
set.add(#b);
```

Or use the Java-like `Set.of()` method:

```joe
var set = Set.of(#a, #b);
```

But sets are usually created using Joe's set literal syntax:

```joe
var empty = {};
var set = {#a, #b};
```

Set literals are allowed to have a trailing comma:

```joe
var set = {
    #a, 
    #b,
};
```

Set member is tested with `contains()`, as in Java:

```joe
var set = {#a, #b};
var flag = set.contains(#a);  // true!
```

## Maps

A [[joe.Map]] is a Java `Map<Object,Object>`, 
key/value store.  It has much the same operations as Java maps.
See the link for the full API.

There are several ways to create and initialize lists.  First,
build it up one entry at a time:

```joe
var map = Map();
map.put(#a, 1);
map.put(#b, 2);
map.put(#c, 3);
```

Or use the Java-like `Map.of(values...)` method:

```joe
var map = Map.of(#a, 1, #b, 2, #c, 3);
```

However, maps are usually created using Joe's map literal syntax:

```joe
var empty = {:};
var map = {#a: 1, #b: 2, #c, 3};
```

Map literals are allowed to have a trailing comma:

```joe
var map = {
  #a: 1,
  #b: 2,
  #c: 3,
};
```

Maps can be queried much as in Java:

```joe
var map = {#a: 1, #b: 2, #c, 3};

map.isEmpty();      // False
map.size();         // 3
map.get(#b);        // 2
```

Maps can also be accessed using array notation:

```joe
var map = {#a: 1, #b: 2, #c, 3};

var c = map[#c];   // map.get(#c);
map[#d] = 4;       // map.put(#d, 4);
```

## Errors

An [[joe.Error]] is a Java `JoeError` exception:
a Joe runtime error, an error thrown explicitly by a Joe script, or
a Joe assertion failure.

Errors are thrown using the `throw` or `assert` statements, and can
be caught using the [[function:joe.catch]] function.

## Functions and Methods

Joe's functions and methods are first class values.  They can be
assigned to variables, passed to functions, and invoked at a later
time.  See the sections on [Functions](functions.md) and
[Classes](classes.md) for more information.

## Classes

Joe scripts can define classes; a class can have:

- Instance variables
- Instance methods
- An initializer
- Static variables
- Static methods
- A static initializer

Joe classes support single inheritance.

Joe class instances can be trivially converted into
[Nero](nero/nero.md) `Facts`.

See the section on [Classes](classes.md) for more information.

## Records

Joe scripts can define record types.  Like Java record types,
a Joe record has an ordered number of immutable fields, and can
have:

- Instance methods
- Static variables
- Static methods
- A static initializer

Joe record values can be trivially converted into 
[Nero](nero/nero.md) `Facts`.

See the section on [Records](records.md) for more information.

## Rule Sets

The `ruleset` keyword introduces a Nero 
[`RuleSet`](library/type.joe.RuleSet.md) literal.

```joe
var rules = ruleset {
    ...
};
```

See the [Nero Datalog](nero/nero.md) documentation for a description of
the Nero language, and [Using Nero with Joe](nero/nero_and_joe.md) for
a discussion of how to use Nero rule sets in Joe scripts.

[^unicode]: Joe supports Unicode escapes, e.g., `\u1234`, as in Java;
but only within string literals.

[^textblocks]: The documentation for Java's text blocks is strewn with
special cases, and is nearly impenetrable.  The Joe implementation is 
simpler and a little more forgiving, relying on Java's 
`String::stripIndent` to do most of the work; and I *think* the result 
is pretty much the same.
