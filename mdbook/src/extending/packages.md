# Joe Packages

A *package* is a collection of Joe functions and types intended to be added
to a `Joe` interpreter as a unit.  Joe's 
[Standard Library](../library/pkg.joe.md) is just such a package, as is
the [Test Tool API](../library/pkg.joe.test.md) provided by `joe test`.

There are two kinds of Joe package: native packages and scripted packages.

- [Native Packages](#native-packages)
- [Defining a Package](#defining-a-package)
- [Installing a Package](#installing-a-package)
- [Registering a Package for Import](#registering-a-package-for-import)

## Native Packages

Native packages are implemented in Java for inclusion in an application or 
library `.jar` file.  Such a package can contain

- [native functions](native_functions.md)
- [registered types](registered_types.md)
- Joe scripts

See [Defining a Package](#defining-a-package) for details.

## Scripted Packages

A scripted package is collection of one or more Joe scripts, placed in a
[Joe Package Repository](../package_repos.md) on the local file system for
general use.  See the link for details.

## Defining a Package

Defining a package is much like defining a [registered type](registered_types.md).

- Subclass `NativePackage`
- Give the package a name
- Add content.

For example, the [Standard Library](../library/pkg.joe.md) package, `joe`, 
is defined like this:

```java
public class StandardLibrary extends JoePackage {
    public static final StandardLibrary PACKAGE = new StandardLibrary();
    ...

    public StandardLibrary() {
        // Define the package name
        super("joe");

        // Add native functions
        function("catch", this::_catch);
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
package is installed each defined global function will be 
installed automatically into the interpreter.
The function implementations are typically placed within the `NativePackage` 
subclass itself, as shown here.

The types are simply [registered types](registered_types.md); when the
package is installed, each listed type will be installed automatically
into the interpreter. The actual proxies
can be defined as nested classes in the `NativePackage` subclass, but are
more typically defined in their own `.java` files.

Finally, a script resource is simply a Joe script found as a resource file 
in the project's jar file, usually adjacent to the package class that 
references it.

Script resources are loaded into the package's environment when the package
is first loaded.  Only exported types and functions are available for
installation or import.

## Installing a Package

To install a package directly into an interpreter, use `Joe::installPackage`:

```java
var joe = Joe();
joe.installPackage(new MyPackage());    // OR
joe.installPackage(MyPackage.PACKAGE);
```

The package's exported functions and types will be immediately available to
scripts.

## Registering a Package for Import

Alternatively, a package can be registered for later 
[import](../statements.md#import-declarations).  Use
`Joe::registerPackage` to register a `NativePackage` for import.

```java
var joe = Joe();
joe.registerPackage(new MyPackage());    // OR
joe.registerPackage(MyPackage.PACKAGE);
```

Packages found in [Joe package repositories](../package_repos.md) are
registered automatically.
