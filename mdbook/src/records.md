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

In most ways, a record type is just like a class.  That is, a record type may 
have:

- methods
- static methods
- static fields
- a static initializer

A record type differs from a class in the following ways:

- Its fields are immutable, and no new fields may be added dynamically.
- Its fields have a well-defined order.
- Instances are initialized automatically; no `init` method is required.
- The record type's `init` method, if it has one, has no special
  semantics; it's just another method.
- A record type cannot extend another type or be extended by another type.

See the [Classes](classes.md) section for more information on methods,
etc.

## Records and Pattern Matching

Record types are especially useful when [pattern matching](patterns.md)
is used.

