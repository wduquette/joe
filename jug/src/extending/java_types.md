# Joe and Java Data Types

Joe supports three kinds of data type:

- [Standard Types](#standard-types)
- [Opaque Types](#opaque-types)
- [Registered Types](#registered-types)
 
## Standard Types

Joe provides a number of standard data types, as described in the
[Types and Values](../types.md) section and the 
[Joe Standard Library](../library/pkg.joe.md) section.  Joe represents these internally
as their normal Java types.

| Joe Type        | Java Type                                                   |
|-----------------|-------------------------------------------------------------|
| [[joe.Boolean]] | [[java:java.lang.Boolean]]                                  |
| [[joe.Error]]   | [[java:com.wjduquette.joe.JoeError]] (a `RuntimeException`) |
| [[joe.Keyword]] | [[java:com.wjduquette.joe.Keyword]]                         |
| [[joe.List]]    | [[java:com.wjduquette.joe.JoeList]] (a `List<Object>`)      |
| [[joe.Number]]  | [[java:java.lang.Double]]                                   |
| [[joe.String]]  | [[java:java.lang.String]]                                   |

When a script passes a value into Java, consequently, 

- A Joe [[joe.List]] is already a Java `List<Object>`. 
- A Joe [[joe.String]] is already a Java `String`.  
- A Joe [[joe.Number]] is already a Java `Double`. 

This greatly decreases the amount of work a binding needs to do when working 
with Joe values, compared to other embedded languages I have used.

A client binding should generally try to make the best use of these
types.

## Opaque Types

Every Java value[^primitive] is a valid Joe value, and 
can be assigned to Joe variables, passed to Joe functions, and returned 
by Joe functions, whether Joe knows anything special about the value's
type or not.

A Java type that Joe knows nothing special about is called an *opaque type*.
But every such type is still a Java class and a subtype of 
`java.lang.Object`, and so "nothing special" is still quite a lot.  
Given such a value, Joe can:

- Determine the name of the Java class to which it belongs
- Convert it to a string for output
- Compare it for equality with other values
- Use it as a key in a hash table
- If it's a `Collection<?>`, iterate over its members

That might be all that's needed for many domain-specific use cases. When more 
is wanted, the client can register a [proxy type](registered_types.md) for 
the type, thus turning the opaque type into a *registered type*.

## Registered Types

A *registered type* is a Java type for which the client has registered
a [proxy type](registered_types.md). The proxy type can give the Joe 
interpreter the ability to:

- Create instances of the type
- Access fields of values of the type
- Call methods on values of the type
- Customize the string representation for the type
- Define static constants and methods for the type
- Make the type iterable, so that its contents can be iterated over via
  the `foreach` statement
- Convert values of the type into Nero `Fact` values

**Note**: Most of the standard Joe types are nothing more than registered 
types that are registered automatically with every Joe interpreter.  There's 
no magic; or, rather, the magic is fully available for use in client bindings.

[^primitive]: Primitive types are represented by their boxed equivalents,
e.g, `double` by `Double`.
