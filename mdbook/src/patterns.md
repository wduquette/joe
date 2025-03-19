# Pattern Matching

Joe supports a rich pattern-matching capability inspired the Rust language's
`let`, `if let`, and `match` statements.

- [The `let` Statement](#the-let-statement)
- [The `if let` Statement](#the-if-let-statement)
- [The `match` Statement](#the-match-statement)
- [Pattern Syntax](#pattern-syntax)

## The `let` Statement

The `let` statement performs a destructuring bind, that is, it binds
variables to values within a data structure.  For example, suppose that the
function `f()` returns a two-item list, and the caller wants to assign the
list items to variables `x` and `y`.

One could do this:

```joe
var result = f();
var x = result[0];
var y = result[1];
```

Or, one could do a destructing bind using `let`:

```joe
let [x, y] = f();
```

Here, `let` matches the list returned by `f()` against the pattern 
`[x, y]` and binds variables `x` and `y` to the matched values.

**`let` vs. `var`**: The `let` statement is quite similar to the
`var` statement; both declare new variables, and these two statements
are semantically equivalent:

```joe
var x = 5;
let x = 5;
```

There are two differences:

- `let` can match more complex patterns, while `var` cannot.
- `var` is faster, as it doesn't incur any pattern-matching overhead.

**When the pattern doesn't match:** If `let`'s pattern doesn't match
the target value, e.g., if `f()` didn't return a two-item list in the
example shown above, then the pattern match will fail and Joe will 
throw a runtime error.  Therefore, `let` should only be used when the 
shape of the target value is known ahead of time.  Use 
[`if let`](#the-if-let-statement) or 
[`match`](#the-match-statement) to test whether a value matches a 
particular pattern.

**Matching Maps:** Similarly, one can match the values of map fields.  Suppose 
`g()` returns a map, and the call wants the values of the map's 
`#name` and `#age` keys:

```joe
// The hard way
var map = g();
var n = map[#name];
var a = map[#age];

// The easy way
let {#name: n, #age: a} = g();
```

If the `map` returned by `g()` has keys `#name` and `#age`, this statement
will bind the variables `n` and `a` to `map[#name]` and `map[#age]`.  And
if not, Joe will throw a runtime error, as before.

These are only a few of the kinds of patterns that Joe supports. See
[Pattern Syntax](#pattern-syntax) for more.

## The `if let` Statement

Not yet implemented.

## The `match` Statement

Not yet implemented.

## Pattern Syntax

Here is a list of the many kinds of pattern Joe supports.  Some patterns
can contain other patterns as subpatterns, allowing a pattern to match
quite complicated data structures.  Here is a list of the different 
kinds of patterns:

- [Binding Variables](#binding-variables)
- [Wildcards](#wildcards)
- [Constants](#constants)
- [Interpolated Expressions](#interpolated-expressions)
- [List Patterns](#list-patterns)
- [Map Patterns](#map-patterns)
- [Matching Instances with Map Patterns](#matching-instances-with-map-patterns)
- [Instance Patterns](#instance-patterns)
- [Record Patterns](#record-patterns)

### Binding Variables

We've seen binding variables in most of the examples shown above.
A binding variable is a variable name that appears within the pattern and
is assigned the corresponding value in the match target.

```joe
let [a, b] = [1, 2];    // a = 1, b = 2.
```

A binding variable can also be used to capture a subpattern.  In the
following example, the variable `b` is assigned list `[2, 3]` while
`c` and `d` are assigned the values of the list's items.

```joe
var list = [1, [2, 3], 4];

let [a, b = [c, d], e];
```

### Wildcards

A *wildcard* is a pattern that matches (and ignores) any value.  A 
wildcard is written as an identifier with leading underscore, e.g., 
`_`, `_ignore`, `_x`.  For example:

```joe
let [x, _] = ["abc", "def"];
```

`x` will be assigned the value `"abc"`, while the second item of 
the target `list` will be ignored.

It's most common to use the wildcard `_`; but using a longer name can
be useful to document what the ignored value is:

```joe
let [first, _last] = ["Joe", "Pro"];
```

Using `_last` indicates that we don't care about the last name
at the moment, but also shows that it is the last name that we are
ignoring.

### Constants

A constant pattern is a constant value included in the pattern; the 
corresponding value in the target must have that exact value.

```joe
function isPro(list) {
    if let ([_, "Pro"] = list1) {
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

### Interpolated Expressions

To use a computed value as a constant, interpolate
it using `$(...)`.  

```joe
var a = 5;
var b = 15;

let [x, $(a + b)] = [10, 20];  // Matches; x == 10.
```

Here, `$(a + b)` evaluates to `20`, which matches the second
item in the target list.

The parentheses may be omitted if the interpolated expression is just
a variable name:

```joe
var wanted = "Pro";

let [first, $wanted] = ["Joe", "Pro"];   
```

### List Patterns

We've seen many list patterns in the above examples.  Syntactically,
a list pattern is simply a list of patterns that matches a `List` of
values.  The matched list must have exactly the same number of items 
as the list pattern, and each subpattern must match the corresponding 
item.

```joe
if let ([a, [b, _], "howdy"] = list) {
    // ...
}
```

The pattern `[]` matches the empty list.

Sometimes the length of the list is unknown; in this case, the list
pattern can provide a pattern variable to bind to the list's tail:

```joe
if let ([a, b : tail] = list) {
    // tail gets the rest of the list.
}
```

The variables `a` and `b` will get `list[0]` and `list[1]`, and `tail`
will get any remaining items, or the empty list if `list.size() == 2`.
(The match will naturally fail if `list.size() < 2`.)

### Map Patterns

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
let {#a: a, #b: b} = {#a: 1, #b: 2, #c: 3};  // a = 1, b = 2
let ($x: value} = someMap;                   // value = someMap.get(x)
let {#a: [a, b, c], #b: x} = someMap;
```

### Matching Instances with Map Patterns

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
let {"id": i, "color": c} = Thing(123, "red");
let {#id: i,  #color: c}  = Thing(123, "red");
```

As when matching `Map` values, the pattern can reference a subset of
the object's fields.

## Instance Patterns

An instance pattern is like a [map pattern](#map-patterns), but it can
match on the value's type as well as on its fields.  For example,

```joe
class Thing {
    method init(id, color) {
        this.id = id;
        this.color = color;
    }
}

// These two statements are equivalent
let Thing{"id": i, "color": c} = Thing(123, "red");
let Thing{#id: i,  #color: c}  = Thing(123, "red");
```

An instance pattern consists of the name of the desired type, followed by
a map pattern for its fields. For the pattern to match:

- The named type must be the target value's type or one of its supertypes.
- The value must have all of the specified fields.
- The field patterns must match.

Types are matched based on their names, i.e., in `let Thing{...} = thing;` the
type will match if `Joe.typeOf(thing).name() == "Thing"`, ***not*** if 
`Joe.typeOf(thing) == Thing`.  In other words, there is no requirement that
the matched type is in scope; it is enough that the value being matched 
knows its type and that its type's name is the name included in the pattern.
See [Unscoped Types](introspection.md#unscoped-types) for more information.

## Record Patterns

Values of [record types](records.md) can be matched by both 
[map patterns](#map-patterns) and [instance patterns](#instance-patterns); but 
because each record type has a fixed number of fields in a known order, record 
types also support a special syntax that makes record types especially suited 
to pattern matching.

```joe
record Person(name, age) {}

var person = Person(Joe, 80);

// These statements are identical
let Person(n, a) = person;
let Person{#name: n, #age: a} = person;
```

The first form can only be used with a value of a record type; it matches
the values of the type's fields in sequence.  All fields must be
represented.  The field subpatterns can be any arbitrary patterns, as
usual.
