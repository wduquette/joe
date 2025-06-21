# Registered Types

A *registered type* is a Java data type for which Joe has been provided
a *proxy type*: an object that provides information about the type.
Most of Joe's standard types, e.g., 
[`String`](../library/type.joe.String.md), are implemented in just this
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

By convention, a Java proxy types have names ending
with "Type", as in `StringType`.  If the type can be subclassed by a scripted
Joe `class`, then the proxy type's name should end in "Class", as in 
`TextBuilderClass`.  If the type has no instances, but exists only as the 
owner of static methods and/or constants, it should end in "Singleton",
as in the `JoeSingleton` class.

The constructor defines the various aspects of the proxy:

- [The Script-level Type Name](#the-script-level-type-name)
- [The Proxied Types](#the-proxied-types)
- [Type Lookup](#type-lookup)
- [The Supertype](#the-supertype)
- [Stringification](#stringification)
- [Iterability](#iterability)
- [Static Constants](#static-constants)
- [Static Methods](#static-methods)
- [Initializer](#initializer)
- [Static Types](#static-types)
- [Instance Methods](#instance-methods)
- [Installing a Proxy Type](#installing-a-type-proxy)

## The Script-level Type Name

First, the proxy defines the script-level type name, which by convention
should always begin with an uppercase letter.  For example:

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
a base class, it might be desirable to explicitly identify the concrete
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

## The Supertype

Sometimes it happens that both a type and its supertype are registered types;
this is the case with Joe's `Error` and `AssertError` types.  In such a case,
the subtype's proxy can "inherit" the supertype's methods via the `supertype()`
method:

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

When Joe converts a value to a string, it does so using a function called
`Joe::stringify`.  If the value belongs to a proxied type, `Joe::stringify`
calls the proxy's `stringify()` method, which returns the result of the
values `toString()` by default.

To change how a value is stringified at the script level, override 
`ProxyType::stringify`.  For example, `NumberType` ensures that 
integer-valued numbers are displayed without a trailing `.0`:

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

## Iterability

Joe's `foreach` statement, and its `in` and `ni` operators,  
can iterate over or search the following kinds of collection values:

- Any Java `Collection<?>`
- Any value that implements the `JoeIterable` interface
- Any proxied type whose `ProxyType` can produce a list of items for iteration.

To make your registered type iterable, provide an iterables supplier, a
function that accepts a value of your type and returns a `Collection<?>`.

```java
public class MyProxyType extends ProxyType<MyType> {
  public MyProxyType() {
    super("MyType");
        ...
        iterables(myValue -> myValue.getItems());
        ...
  }
    ...
}
```

## Static Constants

A proxy may define any number of named constants, to be presented at
the script level as properties of the type object.  For example,
`NumberType` defines several numeric constants.  A constant is defined
by its property name and the relevant Joe value.

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

In this case, the constant is accessible as `Number.PI`.

## Static Methods

A proxy may also define any number of static methods, called as properties
of the type object.  For example, the `String` type defines the 
[`String.join()`](../library/type.joe.String.md#static.join) method, which
joins the items in the list into a string with a given delimiter.

A static method is simply a [native function](native_functions.md) defined
as a property of a list object.

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
        ... create a new list given the arguments ...
        return new ListValue(list);
    }
```

## Static Types

A proxy type can be defined as a kind of library of static constants and
methods.  The `NumberType` is just such a proxy.  Numbers have no methods
in Joe, and they do not need an initializer as they can be typed directly
into a script.  

In this case, the type can be declared to be a *static type*.  Among other
things, this means that attempts to create an instance using the type's
initializer will get a suitable error message.

```java
public class NumberType extends ProxyType<Double> {
    public NumberType() {
        super("Number");
        staticType();
        proxies(Double.class);
        ...
    }
    ...
}
```

In the case of the `Number` type, the proxy still proxies an actual data type.
This need not be the case.  It is common to define a static type as a named
library of functions with no related Java type.  For example, consider
a library to convert between Joe values and JSON strings.  It might look
like this:

```java
public class JSONSingleton extends ProxyType<Void> {
    public JSONSingleton() {
        super("JSON");
        staticType();
        
        staticMethod("toJSON",   this::_toJSON);
        staticMethod("fromJSON", this::_fromJSON);
        ...
    }
    ...
}
```

## Instance Methods

Most type proxies will define one or more instance methods for values of the
type.  For example, [`String`](../library/type.joe.String.md) and 
[`List`](../library/type.joe.List.md) provide a great many instance methods.

An instance method is quite similar to a [native function](native_functions.md), but
an instance method is bound to a value of the proxied type, and so has a 
slightly different signature.  For example, here is the implementation of 
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
The argument's type is type `V`, as defined in the proxy's `extends` clause.
Otherwise, this is nothing more than a native function, and it is implemented
in precisely the same way.

## Installing a Proxy Type

Proxy types are installed using the `Joe::installType` method:

```java
var joe = new Joe();

joe.installType(new MyType());
```

Installation creates the `MyType` object, and registers the type so that
`MyType`'s instance methods can be used with all values of `MyType`.

If a proxy type has no dynamic data, i.e., if it need not be configured with
application-specific data in order to do its job, it is customary to create
a single instance that can be reused with any number of instances of `Joe`.
This is the case for all of the proxies in Joe's standard library.  For example,

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


