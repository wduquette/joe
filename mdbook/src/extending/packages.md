# Joe Packages

A *package* is a collection of Joe functions and types intended to be added
to a `Joe` interpreter as a unit.  Joe's 
[Standard Library](../library/pkg.joe.md) is just such a package, as is
the [Test Tool API](../library/pkg.joe.test.md) provided by `joe test`.

A package can be implemented in Java as a collection of
[native functions](native_functions.md) and
[registered types](registered_types.md), or in Joe as a collection of
Joe [functions](../functions.md) and [classes](../classes.md), or as 
combination of the two.

Joe intentionally provides no means of installing a package at the script
level; it is for the Java client to determine which packages it wishes
to make available.[^import]  Consequently, packages are usually defined
at the Java level even when they are implemented primarily as Joe code.

- [Defining a Package](#defining-a-package)
- [Installing a Package](#installing-a-package)
- [Library Packages vs. Component Packages](#library-packages-vs-component-packages)

## Defining a Package

Defining a package is much like defining a 
[registered type](registered_types.md).

- Subclass `JoePackage`
- Give the package a name
- Add content.

For example, the [Standard Library] package, `joe`, is defined like this:

```java
public class StandardLibrary extends JoePackage {
    public static final StandardLibrary PACKAGE = new StandardLibrary();
    ...

    public StandardLibrary() {
        // Define the package name
        super("joe");

        // Add native functions
        globalFunction("catch", this::_catch);
        ...

        // Add native types
        type(BooleanProxy.TYPE);
        ...
        
        // Include script resources
        scriptResource(getClass(), "pkg.joe.joe");
    }
    ...
}
```

A global function is just a [native function](native_functions.md); when the
package is installed, each defined global function will be 
installed automatically into the interpreter using `Joe::installGlobalFunction`.
The function implementations are typically placed within the `JoePackage` subclass 
itself, as shown here.

The types are simply [registered types](registered_types.md); when the
package is installed, each listed type will be installed automatically
into the interpreter using `Joe::installType`.  The actual proxies
can be defined as nested classes in the `JoePackage` subclass, but are
more typically defined in their own `.java` files.

Finally, a script resource is simply a Joe script found as a resource file 
in the project's jar file, usually adjacent to the package class that 
references it.  At installation, the script resources will be loaded and 
executed after the global functions and native types.

## Installing a Package

To install a package into an interpreter, use `Joe::installPackage`:

```java
var joe = Joe();
joe.installPackage(new MyPackage());    // OR
joe.installPackage(MyPackage.PACKAGE);
```

## Library Packages vs. Component Packages

Reusable libraries, such as Joe's standard library, are usually defined 
as `JoePackage` subclasses, as shown here.

Often, though, a particular component, e.g., `joe test`, will define a
component-specific package of code, which is installed automatically by
the component into the component's own instance of `Joe`.  In this 
case the component might or might not define an explicit `JoePackage` subclass,
as the `JoePackage` is primarily a convenient way for a client to install
reusable bindings.

However, the component API should still be thought of as a package, and
given a package name, as this is how distinct bindings are distinguished
in the documentation produced by the `joe doc` tool.

[^import]: Of course, a client could choose to provide a native function
for importing its own packages....
