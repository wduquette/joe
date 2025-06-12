# Pattern Matching

Joe supports a rich pattern-matching capability inspired by the Rust language's
similar capability. Joe's 
[`~` matching operator](operators.md#matching-operator)
and the 
[`var`](statements.md#variable-declarations),
[`foreach`](statements.md#foreach-statements), and
[`match`](statements.md#match-statements) statements all make
use of pattern matching to do destructuring binds.

Joe supports a rich variety of patterns.  Some of them can contain
other patterns as subpatterns, allowing a pattern to match
quite complicated data structures.

- [Patterns and Destructuring Binds](#patterns-and-destructuring-binds)
- [Binding Variables](#binding-variables)
- [Wildcards](#wildcards)
- [Constants](#constants)
- [Interpolated Expressions](#interpolated-expressions)
- [List Patterns](#list-patterns)
- [Map Patterns](#map-patterns)
- [Matching Instances with Map Patterns](#matching-instances-with-map-patterns)
- [Named-Field Patterns](#named-field-patterns)
- [Record Patterns](#record-patterns)

## Patterns and Destructuring Binds

A destructuring bind is a way to bind one more variables to values within
a complex data structure.  The bind makes use of a pattern that duplicates 
the structure of the target value to match variable names to specific elements.

For example, suppose that the function `f()` returns a two-item list, and the 
caller wants to assign the list items to the variables `x` and `y`.

One could do this:

```joe
var result = f();
var x = result[0];
var y = result[1];
```

Or, one could do a destructing bind using `var`:

```joe
var [x, y] = f();
```

Here, `var` matches the list returned by `f()` against the pattern 
`[x, y]` and binds variables `x` and `y` to the matched values.

## Binding Variables

We've seen binding variables in most of the examples shown above.
A binding variable is a variable name that appears within the pattern and
is assigned the corresponding value in the match target.

```joe
var [a, b] = [1, 2];    // a = 1, b = 2.
```

A binding variable can also be used to capture a subpattern.  In the
following example, the variable `b` is assigned list `[2, 3]` while
`c` and `d` are assigned the values of the list's items.

```joe
var list = [1, [2, 3], 4];

var [a, b = [c, d], e];
```

## Wildcards

A *wildcard* is a pattern that matches (and ignores) any value.  A 
wildcard is written as an identifier with leading underscore, e.g., 
`_`, `_ignore`, `_x`.  For example:

```joe
var [x, _] = ["abc", "def"];
```

`x` will be assigned the value `"abc"`, while the second item of 
the target `list` will be ignored.

It's most common to use the wildcard `_`; but using a longer name can
be useful to document what the ignored value is:

```joe
var [first, _last] = ["Joe", "Pro"];
```

Using `_last` indicates that we don't care about the last name
at the moment, but also shows that it is the last name that we are
ignoring.

## Constants

A constant pattern is a constant value included in the pattern; the 
corresponding value in the target must have that exact value.

```joe
function isPro(list) {
    if (list ~ [_, "Pro"]) {
        return true;
    } else {
        return false;
    }
}

var x = isPro(["Joe", "Pro"]);       // Returns true
var y = isPro(["Joe", "Amateur"]);   // Returns false
```

The constant must be a literal 
[string](types.md#strings),
[number](types.md#numbers),
[boolean](types.md#booleans),
[keyword](types.md#keywords),
or `null`.

## Interpolated Expressions

To use a computed value as a constant, interpolate
it using `$(...)`.  

```joe
var a = 5;
var b = 15;

var [x, $(a + b)] = [10, 20];  // Matches; x == 10.
```

Here, `$(a + b)` evaluates to `20`, which matches the second
item in the target list.

The parentheses may be omitted if the interpolated expression is just
a variable name:

```joe
var wanted = "Pro";

var [first, $wanted] = ["Joe", "Pro"];   
```

## List Patterns

We've seen many list patterns in the above examples.  Syntactically,
a list pattern is simply a list of patterns that matches a `List` of
values.  The matched list must have exactly the same number of items 
as the list pattern, and each subpattern must match the corresponding 
item.

```joe
if (list ~ [a, [b, _], "howdy"]) {
    // ...
}
```

The pattern `[]` matches the empty list.

Sometimes the length of the list is unknown; in this case, the list
pattern can provide a pattern variable to bind to the list's tail:

```joe
if (list ~ [a, b : tail]) {
    // tail gets the rest of the list.
}
```

The variables `a` and `b` will get `list[0]` and `list[1]`, and `tail`
will get any remaining items, or the empty list if `list.size() == 2`.
(The match will naturally fail if `list.size() < 2`.)

## Map Patterns

A map pattern matches objects with keys and values, e.g., 
[`Map`](library/type.joe.Map.md) values. 

- The keys must be [constants](#constants)
- The values can be any pattern.
- The target `Map` must contain all of the keys listed in the 
  pattern, and their values must match the corresponding value patterns.
- The target `Map` can contain any number of keys that don't appear in 
  the pattern.

Some examples:

```joe
var {#a: a, #b: b} = {#a: 1, #b: 2, #c: 3};  // a = 1, b = 2
var ($x: value} = someMap;                   // value = someMap.get(x)
var {#a: [a, b, c], #b: x} = someMap;
```

## Matching Instances with Map Patterns

A map pattern can also match any Joe value with field properties, e.g.,
an instance of a Joe `class`.  The pattern's keys match the field names
and the pattern's values match the field values.

- Key patterns must be string or Keyword [constants](#constants) that 
  correspond to the field names, or interpolated expressions that evaluate 
  to such strings or keywords.

```joe
class Thing {
    method init(id, color) {
        this.id = id;
        this.color = color;
    }
}

// These two statements are equivalent
var {"id": i, "color": c} = Thing(123, "red");
var {#id: i,  #color: c}  = Thing(123, "red");
```

As when matching `Map` values, the pattern can reference a subset of
the object's fields.

## Named-Field Patterns

A named-field pattern matches the type and field values for any 
Joe value with named fields. 

```joe
class Thing {
    method init(id, color) {
        this.id = id;
        this.color = color;
    }
}

var Thing(id: i, color: c) = Thing(123, "red");
```

A named-field pattern consists of the name of the desired type, followed by
field-name/pattern pairs in parentheses.

- The named type must be the target value's type or one of its supertypes.
- The value must have all of the specified fields.
- The field patterns must match the field values.

Types are matched based on their names, i.e., in `var Thing(...) = thing;` the
type will match if `Joe.typeOf(thing).name() == "Thing"`, ***not*** if 
`Joe.typeOf(thing) == Thing`.  In other words, there is no requirement that
the matched type is in scope; it is enough that the value being matched 
knows its type and that its type's name is the name included in the pattern.
See [Unscoped Types](introspection.md#unscoped-types) for more information.

## Record Patterns

Values of [record types](records.md) can be matched by both 
[map patterns](#map-patterns) and [named-field patterns](#named-field-patterns); but 
because each record type has a fixed number of fields in a known order, record 
types also support a special syntax that makes record types especially suited 
to pattern matching.

```joe
record Person(name, age) {}

var person = Person(Joe, 80);

// These statements are identical
var Person(n, a) = person;
var Person{#name: n, #age: a} = person;
```

The first form can only be used with a value of a record type; it matches
the values of the type's fields in sequence.  All fields must be
represented.  The field subpatterns can be any arbitrary patterns, as
usual.
