# Registered Types

A *registered type* is a Java data type for which the client has registered
a *proxy type* with the Joe interpreter.  A proxy type is an object
that provides information about the instances of the type.
Most of Joe's standard types, e.g., [`String`](../library/type.joe.String.md), are implemented in just this
way.  This section explains how to define a `ProxyType<V>` and register
it with Joe for use.

## Defining A Proxy Type

A proxy type is a subclass of `ProxyType<V>`, where `V` is the proxied type.
For example,

```java
public class StringType extends ProxyType<String> {
    public StringType() {
        super("String");
    }
    ...
}
```

By convention, Java proxy type names end with "Type", e.g., `StringType`.

- If the type can be subclassed by a scripted Joe `class`, the name should
  end with "Class", e.g., `TextBuilderClass`.
- If the type has no instances, i.e., if it exists only as the owner of 
  static methods and/or constants, its name should end with "Singleton", 
  e.g., `JoeSingleton`.

The proxy type defines the various aspects of the proxy.  Most details
are configured in the type's constructor; others involve overriding
various `ProxyType` methods.

- [The Script-level Type Name](#the-script-level-type-name)
- [The Proxied Types](#the-proxied-types)
- [Type Lookup](#type-lookup)
- [Extending Supertype Proxies](#the-supertype)
- [Stringification](#stringification)
- [Static Constants](#static-constants)
- [Static Methods](#static-methods)
- [Static Types](#static-types)
- [Initializer](#initializer)
- [Iterability](#iterability)
- [Instance Fields](#instance-fields)
- [Instance Methods](#instance-methods)
- [Nero Fact Conversion](#nero-fact-conversion)
- [Installing a Proxy Type](#installing-a-type-proxy)

## The Script-level Type Name

First, the proxy's constructor defines the script-level type name, which by 
convention should always begin with an uppercase letter.  For example:

```java
public class StringType extends ProxyType<String> {
    public StringType() {
        super("String");
        ...
    }
    ...
}
```

The script-level type name is often the same as the Java class name, but not 
always. 

- Joe's `Number` type is actually a Java `Double`; it's called `Number` because
  there's only one kind of number in Joe.
- Joe's `List` type actually maps to two different `List<Object>` types, both
  under the umbrella of the `JoeList` interface.  Calling it simply `List` is a
  kindness to the client.

When the proxy is registered Joe will create a global variable with the 
same name as the type, e.g., `String`.

## The Proxied Types

Second, the proxy must explicitly identify the proxied type or types.

Usually a proxy will proxy the single type `V`, but if `V` is an interface or
a base class then it might be desirable to explicitly identify the concrete
classes.  For example, a Joe `String` is just exactly a Java `String`.

```java
public class StringType extends ProxyType<String> {
    public StringType() {
        super("String");
        proxies(String.class);
    }
    ...
}
```

But a Joe `List` could be a Java `ListValue` or `ListWrapper`, both of which
implement the `JoeList` interface:

```java
public class ListType extends ProxyType<JoeList> {
    public ListType() {
        super("List");
        proxies(ListValue.class);
        proxies(ListWrapper.class);
    }
    ...
}
```

## Type Lookup

At runtime, Joe sees a value and looks up the value's proxy type 
in its type registry.  This section explains how
the lookup is done, as it can get complicated.

Joe keeps registered type information in the `proxyTable`, a map from 
Java `Class` objects to Java `ProxyType<?>` objects.

- If value's `Class` is found in the `proxyTable`, the proxy is returned 
  immediately.  This is the most common case.

- Next, Joe looks in the `proxyTable` for the value's superclass, and so on
  up the class hierarchy.

- Next, Joe looks in the `proxyTable` for any interfaces implemented by the
  value's type, starting with the type's own `Class` and working its way
  up the class hierarchy.

- Finally, if no proxy has been found then an `OpaqueType` proxy is created and 
  registered for the value's `Class`.

Whatever proxy is found, it is cached back into the `proxyTable` for the
value's concrete class so that it will be found immediately next time.

**NOTE**: when looking for registered interfaces, Joe only looks at the 
interfaces directly implemented by the value's class or its superclasses; 
it does *not* check any interfaces that those interfaces might extend.
This is intentional, as expanding the search is too likely to lead to
false positives and much unintentional comedy.  Registered interfaces
should be used with great care!

## Extending Supertype Proxies

Sometimes it happens that both a Java type and its Java supertype are 
registered types; this is the case with Joe's `AssertError` and `Error` 
types, which are represented internally by the Java `AssertError` and 
`JoeError` types.  In such a case, the subtype's proxy can "inherit" the 
supertype's methods via the `extendsProxy()` method:

```java
public class AssertErrorProxy extends ProxyType<AssertError> {
    public AssertErrorProxy() {
        super("AssertError");
        proxies(AssertError.class);
        extendsProxy(ErrorProxy.TYPE);
    }
    ...
}
```

## Stringification

Joe doesn't use `Object::toString` when it needs to convert a value to
a string; instead it calls `Joe::stringify`, which allows Joe to customize
the value's script-level string representation.  

For example, all Joe numbers are java `Doubles`. The default string 
representation for doubles includes the fractional part, but Joe includes
the fractional part only when it is non-zero.

For proxied types, `Joe::stringify` calls the proxy's `stringify` method,
which calls `Object::toString` by default.  The client can override 
the proxy's `stringify` method to customize the returned value. 

For example, `NumberType`'s `stringify` method removes the decimal part
when it is zero.

```java
@Override
public String stringify(Joe joe, Object value) {
    assert value instanceof Double;

    String text = ((Double)value).toString();
    if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
    }
    return text;
}
```

## Static Constants

A proxy may define any number of named constants, to be presented at
the script level as properties of the type object.  For example,
`NumberType` defines several numeric constants.  A constant is defined
by its property name and the relevant Joe value using the `constant` method.

```java
public class NumberType extends ProxyType<Double> {
    public NumberType() {
        super("Number");
        ...
        constant("PI", Math.PI);
        ...
    }
    ...
}
```

The constant is accessible as `Number.PI`.

## Static Methods

A proxy may also define any number of static methods, called as properties
of the type object.  For example, the `String` type defines the 
[`String.join()`](../library/type.joe.String.md#static.join) method, which
joins the items in the list into a string with a given delimiter,
and the `Number` type defines a great many math functions as static methods.

A static method is simply a [native function](native_functions.md) defined
as a property of a list object using the `staticMethod` method:

```java
public class StringType extends ProxyType<String> {
    public StringType() {
        ...
        staticMethod("join", this::_join);
        ...
    }
    ...
    
    private Object _join(Joe joe, Args args) {
        args.exactArity(2, "join(delimiter, list)");
        ...
    }
}
```

## Static Types

A proxy type can be defined as a kind of library of static constants and
methods.  `JoeSingleton` is just such a proxy type.  There are no 
instances of `Joe` at the script level, and so there are instance fields or
methods.

In this case, the type can be declared to be a *static type*.  Among other
things, this means that attempts to create an instance using the type's
initializer will get a suitable error message.

```java
public class JoeSingleton extends ProxyType<Void> {
    public JoeSingleton() {
        super("Joe");
        staticType();
        ...
    }
    ...
}
```

## Initializer

Most type proxies will provide an initializer function for creating values of
the type.  The initializer function is named after the type, returns a value
of the type, and may take any desired arguments.  

For example, the `List` type provides this 
[initializer](../library/type.joe.List.md#init):

```java
public class ListType extends ProxyType<JoeList> {
    public ListType() {
        super("List");
        ...
        initializer(this::_init);
        ...
    }

    private Object _init(Joe joe, Args args) {
        ...
        // Create a copy of another list.
        var other = args.next();
        return new ListValue(other);
    }
    ...
}
```

## Iterability

Joe's `foreach` statement, and its `in` and `ni` operators, can iterate over or 
search the following kinds of collection values:

- Any Java `Collection<?>`
- Any proxied type whose `ProxyType` can produce a list of items for iteration.

To make your registered type iterable, provide an iterable supplier, a
function that accepts a value of your type and returns a `Collection<?>`.

```java
public class MyProxyType extends ProxyType<MyType> {
    public MyProxyType() {
        super("MyType");
        ...
        iterableSupplier(this::_iterables);
        ...
    }
    
    private Object _iterables(Joe joe, MyType value) {
        return value.getItems();
    }
}
```

## Instance Fields

A proxy type can expose a value's properties as read-only fields at the script 
level using the `field` method.  For example, suppose `MyType` has an `id` 
property and the client wishes to expose that property as a read-only field 
rather than as an instance method:

```java
public class MyProxyType extends ProxyType<MyType> {
    public MyProxyType() {
        super("MyType");
        ...
        field(this::_id);
        ...
    }

    private Object _id(Joe joe, MyType value) {
        return value.getId();
    }
}
```

This technique is appropriate if all instances of the type have the same
fields, and the fields are all read-only, which is the usual case.  

In unusual cases it is necessary to override the following `ProxyType` methods:

- `getFieldNames(Object value)`
- `get(Joe joe, Object value, String propertyName)`
- `set(Joe joe, Object value, String fieldName, Object other)`
 
This is an advanced move; see the `com.wjduquette.joe.types.FactType` for an 
example.

## Instance Methods

Most type proxies will define one or more instance methods for values of the
type.  For example, [`String`](../library/type.joe.String.md) and 
[`List`](../library/type.joe.List.md) provide a great many instance methods.

An instance method is like a [native function](native_functions.md), but has a different
signature because it is bound to a value of the proxied type.
For example, here is the implementation of 
the `String` type's [`length()`](../library/type.joe.String.md#method.length) method.

```java
public class StringType extends ProxyType<String> {
    public StringType() {
        ...
        method("length", this::_length);
        ...
    }
    ...

    private Object _length(String value, Joe joe, Args args) {
        args.exactArity(0, "length()");
        return (double)value.length();
    }
}
```

Each instance method takes an initial argument that receives the bound value.
The argument's type is of type `V`, as defined in the proxy's `extends` clause.
Otherwise, this is nothing more than a native function, and it is implemented
in precisely the same way.

## Nero Fact Conversion

Scripted values that are to be used as inputs to 
[Nero rule sets](../nero/nero.md) must first be converted to `Fact` values.
The conversion done automatically, but the type must support the conversion by
overriding the `ProxyType`'s `isFact(Joe, Object)` and `toFact(Joe, Object)` 
methods.

- `isFact(Joe, Object)` indicates whether the object (a value of type `V`) can
  be converted into a `Fact`.
- `toFact(Joe, Object)` actually does the conversion.

If a `ProxyType` defines script-visible [instance fields](#instance-fields) 
using the `ProxyType::field` method, then `isFact` and `toFact` are defined
automatically.  `toFact` will produce a `RecordFact` whose relation is the type
name and whose fields are the type's visible fields. 

However, proxy types are free to override these, to provide `Facts` in some
other way.

## Installing a Proxy Type

Proxy types are installed using the `Joe::installType` method (or by
including them in a [Joe Package](packages.md)):

```java
var joe = new Joe();

joe.installType(new MyType());
```

Installation creates the `MyType` object, and registers the type so that
`MyType`'s instance methods can be used with all values of `MyType`.

If a proxy type has no dynamic data, i.e., if it need not be configured with
application-specific data in order to do its job, it is customary to create
a single instance that can be reused with any number of instances of `Joe`.
This is the case for most of the proxies in Joe's standard library.  For example,

```java
public class StringType extends ProxyType<String> {
    public static final StringType TYPE = new StringType();
    ...
}
```

This can then be installed as follows:

```java
var joe = new Joe();

joe.installType(StringType.TYPE);
```
