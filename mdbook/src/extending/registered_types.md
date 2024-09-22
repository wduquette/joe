# Registered Types

A *registered type* is a Java data type for which Joe has been provided
a *type proxy*: an object that provides information about the type.
Most of Joe's standard types, e.g., 
[`String`](../library/type.joe.String.md), are implemented in just this
way.  This section explains how to define a `TypeProxy<V>` and register
it with Joe for use.

## Defining A Type Proxy

A type proxy is a subclass of `TypeProxy<V>`, where `V` is the proxied type.
For example,

```java
public class StringProxy extends TypeProxy<String> {
    public StringProxy() {
        ...
    }
    ...
}
```

The constructor defines the various aspects of the proxy.

- [The Script-level Type Name](#the-script-level-type-name)
- [The Proxied Types](#the-proxied-types)
- [Static Constants](#static-constants)
- [Static Methods](#static-methods)
- [Initializer](#initializer)
- [Static Types](#static-types)
- [Instance Methods](#instance-methods)
- [Installing a Type Proxy](#installing-a-type-proxy)

## The Script-level Type Name

First, the proxy defines the script-level type name, which by convention
should always begin with an uppercase letter.  For example:

```java
public class StringProxy extends TypeProxy<String> {
    public StringProxy() {
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

Second, the proxy must explicitly identify the proxied type or types, which
must be genuine classes, not interfaces. 

Usually a proxy will proxy the single type `V`, but if `V` is an interface
the relevant classes need to be identified. Here's the first case:

```java
public class StringProxy extends TypeProxy<String> {
    public StringProxy() {
        super("String");
        proxies(String.class);
    }
    ...
}
```

And here's an example of the second.  `JoeList` is an interface, so we must
tell Joe which types the interpreter should look for.

```java
public class ListProxy extends TypeProxy<JoeList> {
    public ListProxy() {
        super("List");
        proxies(ListValue.class);
        proxies(ListWrapper.class);
    }
    ...
}
```

## Static Constants

A proxy may define any number of named constants, to be presented at
the script level as properties of the type object.  For example,
`NumberProxy` defines several numeric constants.  A constant is defined
by its property name and the relevant Joe value.

```java
public class NumberProxy extends TypeProxy<Double> {
    public NumberProxy() {
        super("Number");
        ...
        constant("E", Math.E);
        ...
    }
    ...
}
```

In this case, the constant is accessible as `Number.E`.

## Static Methods

A proxy may also define any number of static methods, called as properties
of the type object.  For example, the `String` type defines the 
[`String.join()`](../library/type.joe.String.md#static.join) method, which
joins the items in the list into a string with a given delimiter.

A static method is simply a [native function](native_functions.md) defined
as a property of a list object.

```java
public class StringProxy extends TypeProxy<String> {
    public StringProxy() {
        ...
        staticMethod("join", this::_join);
        ...
    }
    ...
    
    private Object _join(Joe joe, ArgQueue args) {
        Joe.exactArity(args, 2, "join(delimiter, list)");
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
public class ListProxy extends TypeProxy<JoeList> {
    public ListProxy() {
        super("List");
        ...
        initializer(this::_init);
        ...
    }

    private Object _init(Joe joe, ArgQueue args) {
        return new ListValue(args.asList());
    }
```

## Static Types

A type proxy can be defined as a kind of library of static constants and
methods.  The `NumberProxy` is just such a proxy.  Numbers have no methods
in Joe, and they do not need an initializer as they can be typed directly
into a script.  

In this case, the type can be declared to be a *static type*.  Among other
things, this means that attempts to create an instance using the type's
initializer will get a suitable error message.

```java
public class NumberProxy extends TypeProxy<Double> {
    public NumberProxy() {
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
public class JSONProxy extends TypeProxy<Void> {
    public JSONProxy() {
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

An instance method is quite similar to a [native function](native_functions.md);
the only practical difference is that an instance method is bound to a value
of the proxied type, and so has a slightly different signature.  For example,
here is the implementation of the `String` type's 
[`length()`](../library/type.joe.String.md#method.length) method.

```java
public class StringProxy extends TypeProxy<String> {
    public StringProxy() {
        ...
        method("length", this::_length);
        ...
    }
    ...

    private Object _length(String value, Joe joe, ArgQueue args) {
        Joe.exactArity(args, 0, "length()");
        return (double)value.length();
    }
}
```

Each instance method takes an initial argument that receives the bound value.
The argument's type is type `V`, as defined in the proxy's `extends` clause.
Otherwise, this is nothing more than a native function, and it is implemented
in precisely the same way.

## Installing a Type Proxy

Type proxies are installed using the `Joe::installType` method:

```java
var joe = new Joe();

joe.installType(new MyTypeProxy());
```

Installation creates the `MyType` object, and registers the type so that
`MyType`'s instance methods can be used with all values of `MyType`.

If a type proxy has no dynamic data, i.e., if it need not be configured with
application-specific data in order to do its job, it is customary to create
a single instance that can be reused with any number of instances of `Joe`.
This is the case for all of the proxies in Joe's standard library.  For example,

```java
public class StringProxy extends TypeProxy<String> {
    public static final StringProxy TYPE = new StringProxy();
    ...
}
```

This can then be installed as follows:

```java
var joe = new Joe();

joe.installType(StringProxy.TYPE);
```


