# Records

Joe supports record types, similar to Java's record types.

```joe
record Person(first, last) {
    method fullName() {
        return this.first + " " + this.last;
    }
    ...
}

var joe = Person("Joe", "Pro");
println(joe.first);         // Prints "Joe"
println(joe.last);          // Prints "Pro"
println(joe.fullName());    // Prints "Joe Pro"
println(joe);               // Prints "Person(Joe, Pro)"
```

Like a Java record, a Joe record is immutable, having only the fields
defined in the record declaration.

Like Joe [classes](classes.md), a record type is a first-class value that
may be declared in any scope, passed to functions, assigned to variables,
and so forth.

- [Exported Records](#exported-records)
- [Records vs. Classes](#records-vs-classes)
- [Records and Facts](#records-and-facts)
 
## Exported Records

A [Joe package](extending/packages.md) can export record types for
later import.  Such types must be defined using the `export` prefix:

```joe
export record MyExportedRecord(...) { ... }
```

## Records vs. Classes

In most ways, a record type is just like a class.  That is, a record type may 
have:

- methods
- static methods
- static fields
- a static initializer
- a `toString()` method

A record type differs from a class in the following ways:

- Its fields are immutable, and no new fields may be added dynamically.
- Its fields have a well-defined order.
- Instances are initialized automatically; no `init` method is required.
- The record type's `init` method, if it has one, has no special
  semantics; it's just another method.
- A record type cannot extend another type or be extended by another type.

See the [Classes](classes.md) section for more information on methods,
etc.

## Records and Facts

Any record value can be converted into a Nero fact by the
[[static:joe.Joe.toFact]] method. The resulting fact will be a "pair" fact.

