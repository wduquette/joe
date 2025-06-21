# Joe and Java Data Types

Joe supports three kinds of data type:

- [Standard Types](#standard-types)
- [Opaque Types](#opaque-types)
- [Registered Types](#registered-types)
 
## Standard Types

Joe provides a number of standard data types, as described in the
[Types and Values](../types.md) section and the 
[Joe Standard Library](../library/pkg.joe.md) section.  Values of these
types are represented internally as normal Java values.

| Joe Type  | Java Type                                            |
|-----------|------------------------------------------------------|
| `Boolean` | `java.lang.Boolean`                                  |
| `Error`   | `com.wjduquette.joe.JoeError` (a `RuntimeException`) |
| `Keyword` | `com.wjduquette.joe.Keyword`                         |
| `List`    | `com.wjduquette.joe.JoeValue` (a `List<Object>`)     |
| `Number`  | `java.lang.Double`                                   |
| `String`  | `java.lang.String`                                   |

When used in Java code, consequently, a Joe `List` is a Java 
`List<Object>`. A Joe string is a Java `String`.  A Joe `Number` is a 
Java `double`. This greatly decreases the amount of work a binding 
needs to do when working with Joe values, compared to other embedded
languages I have used.

A client binding should generally try to make the best use of these
types.

## Opaque Types

Every Java value, without exception[^primitive], is a valid Joe value, and 
can be assigned to Joe variables, passed to a Joe functions, and returned 
by Joe functions, whether Joe knows anything special about the value's
type or not.

A Java type that Joe knows nothing special about is called an *opaque type*.
But every such type is still a Java class and a subtype of 
`java.lang.Object`, and so "nothing special" is still quite a lot.  
Given such a value, Joe can:

- Determine the name of class it belongs to
- Convert it to a string for output
- Compare it for equality with other values
- Use it as a key in a hash table
- If it's a `Collection<?>`, iterate over its members

That might be all that's needed for many domain-specific use cases. When more 
is wanted, the client can register a [proxy type](registered_types.md) for 
the type, thus turning the opaque type into a registered type.

## Registered Types

A *registered type* is a Java type for which the client has registered
a [proxy type](registered_types.md). The proxy type can give the Joe 
interpreter the ability to:

- Create instances of the type
- Call methods on values of the type
- Access fields of values of the type
- Customize the string representation for the type
- Define static constants and methods for the type
- Make the type iterable, so that its contents can be iterated over via
  the `foreach` statement.

**Note**: Most of the standard Joe types are nothing more than registered 
types that are registered automatically with every Joe interpreter.  There's 
no magic; or, rather, the magic is fully available for use in client bindings.

[^primitive]: Primitive types are represented as their boxed equivalents,
e.g, `double` by `Double`.
