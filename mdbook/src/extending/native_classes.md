# Extending Native Types in Joe

This section explains how to implement a registered type so that it can
be extended by a scripted Joe [`class`](../classes.md).  For example,
Joe's [`TextBuilder`](../library/type.joe.TextBuilder.md) and
[`StackPane`](../library/type.joe.win.StackPane.md) types are Java classes
with [`ProxyTypes`](registered_types.md) that can be extended by Joe
classes.

```joe
class MarkdownBuffer extends TextBuilder {
    function h3(level, text) {
        this.println("\n### " + text);
    }
    ...
}
```

Suppose you have a java class, `Thing`. You wish to write a Joe binding
for `Thing` that allows `Thing` to be extended by Java classes. There are 
three steps:

1. Implement a `ProxyType` for `Thing` in the 
   [usual way](registered_types.md).
2. Define a subclass of `Thing` that implements `JoeInstance`.
3. Make `Thing`'s `ProxyType` extensible.


## Subclass `JoeInstance`

A `JoeInstance` is a type that knows its `JoeClass` and owns a
field map, a map from `String` to `Object`; this allows Joe to treat
it like an instance of a scripted class.

Typically, one simply subclasses the desired class with the following
boilerplate:

```java
public class ThingInstance extends Thing implements JoeInstance {
    private final JoeClass joeClass;
    private final Map<String,Object> fieldMap = new HashMap<>();

    public ThingInstance(JoeClass joeClass) {
        this.joeClass = joeClass;
    }

    @Override public JoeClass getJoeClass() { return joeClass; }
    @Override public Map<String, Object> getInstanceFieldMap() { return fieldMap; }
}
```

Naturally, one can also add other features; 
[`TextBuilder`](../library/type.joe.TextBuilder.md), for example, is a new type
that wraps (rather than subclassing) the Java `StringBuilder` type; it was
designed to be a `JoeInstance` from the ground up.

## Make the `ProxyType` extensible

To make the `ProxyType` extensible, override the `canBeExtended` and `make`
methods as follows:

```java
@Override
public boolean canBeExtended() {
    return true;
}

@Override
public Object make(Joe joe, JoeClass joeClass) {
    return new ThingInstance(joeClass);
}
```

That's it.
