# Introspection

*Introspection*, also known as *reflection*, is the ability for a script to
query metadata about its types and values.  This section discusses Joe's
introspection features, which are available via the methods of the
[`Joe` singleton](library/type.joe.Joe.md).

- [Finding a value's type](#finding-a-values-type)
- [Scoped vs. Unscoped Types](#scoped-vs-unscoped-types)
- [Unscoped Types and Pattern Matching](#unscoped-types-and-pattern-matching)
- [Field Names](#field-names) 

## Finding a value's type

Given any *value* whatsoever, `Joe.typeOf(value)` will return its type.

Given the *type*, `type.name()` will return the type's name.

## Scoped vs. Unscoped Types

Most Joe types are defined in the global environment as a variable with
the type's name whose value is the type itself.

- E.g., the standard type `String` is referenced by the global 
  variable `String`.

A Joe class is defined in the scope that contains the `class` declaration.  If
`class Thing {...}` appears at global scope, it will define a global 
variable called `Thing`.  If `class Thing {...}` appears in a local scope,
e.g., in a function body, it will define a variable called `Thing` in that
scope.

A type defined in a scope is called a *scoped type*.

But consider this case:

```java
function makeValue() {
    class MyType { ... }
    return MyType();
}

var theValue = makeValue();
```

The function `makeValue` creates a class called `MyType` in its own scope and
then returns an instance of the class.  The variable `MyType` goes out of
scope when `makeValue` returns; the type can no longer be accessed "by name"
in any scope in the script.  `MyType` is now said to be *unscoped*.

And yet, `theValue` still has type `MyType`:

```joe
// Prints "MyType"
println(Joe.typeOf(theValue).name());
```

Similarly, just as a client can extend Joe with a binding for a scoped type
(in exactly the same way as standard types like `String` and `List`), a
client can also create values of named-but-unscoped types.

## Unscoped Types and Pattern Matching

Joe's [pattern matching](patterns.md) feature provides several ways to match
on a value's type. In each case, the type is matched by its name rather than
by strict equality of type objects, precisely because the value's type 
might be unscoped: the type's name is known to the value and to the script 
author, but isn't assigned to any convenient variable.

In other words,

```joe
if let (Thing{#id: id} = thing) {
    ...
}
```

will match if `Joe.typeOf(thing).name() == "Thing"`.  Whether type `Thing`'s
variable is in-scope is irrelevant.

## Field Names

The `Joe.getFieldNames(value)` method will return a list of the value's fields.
For example, 

```joe
class Thing {
    method init(id, name) {
        this.id = id;
        this.name = name;
    }
}

// Prints ["id", "name"]
println(Joe.getFieldNames(Thing));
```

If the given value is a Joe type, `Joe.getFieldNames()` will return the 
names of the type's static fields, not the names of its instance fields.

It may seem odd to call `Joe.getFieldNames()` on a value to discover the
value's fields, rather than on the value's type; but Joe types don't 
generally know the names of their instances' fields.  Consider the 
following example:

```
class Person { 
    method init(name) {
        this.name = name; 
    } 
}

var fred = Person("Fred");
fred.favoriteFlavor = "chocolate";
fred.lastYearsFavorite = "vanilla";
```

Every `Person` is assigned a `name` in the initializer; but the script
author has decided to add two additional fields to `Person` `fred`.
A script can add any number of fields to any class instance, fields
other instances of that type won't have.

In fact, the `Person` type doesn't even know that every `Person` has
a `name`.  The `init()` method could be written to assign a value to 
`this.name` for some persons but not others.

Thus, there's no way to query type `Person` to find the field names of its
instances.
