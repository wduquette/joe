# Change Log

## Changes in 0.7.0 (in development)

- Pending

## Changes in 0.6.0 (28 March 2025) 

- Language
  - Lists may now be created with list literal syntax, e.g., 
    `[1, 2, 3]`.
    - List items may be any arbitrary expression.
  - Maps may now be created with map literal syntax, e.g., 
    `{#a: 1, #b: 2, #c: 3}`.
    - Map keys and values may be any arbitrary expressions.
  - Both lists and maps may now be accessed using array notation, e.g.,
    `var x = myList[i]`.
    - Array notation is sugar for the `List` `get` and `set` methods, and
      for the `Map` `get` and `put` methods.
  - Added the `let` statement, which performs destructuring binds using
    [pattern matching](patterns.md).
  - Added scripted [`record` types](records.md).
  - Added the `if let` statement, which performs conditional destructuring
    binds.
  - Added the `match` statement, which matches a target value against a 
    number of case patterns and executes the selected statement.
- Internals/Embedding/Extending
  - Refactored the names of Joe's type-related interfaces and Java classes
    for clarity.
  - Added infrastructure for creating Joe bindings for Java `record` types
    See `CatchResult` for an example.
    - The infrastructure can also be used with non-`record` types that should
      look like records at the script level, i.e., should have an immutable
      ordered list of immutable fields.
    - Added `JoeType::isRecordType` to support this infrastructure.
- Standard Library
  - Added the [`Type`](library/type.joe.Type.md) type as the root of the 
    Joe type system.
  - Added the [`Joe`](library/type.joe.Joe.md) type as the host of Joe's
    introspection (aka reflection) API.  Also, moved several global functions
    to be static methods of `Joe`:
    - `Joe.compare()`
    - `Joe.currentTimeMillis()` (replacing the global `millis()` function)
    - `Joe.stringify()`
  - Added better support for opaque types.
    - Values of opaque types are now provided with an _ad hoc_ proxy that 
      provides a `toString()` method.
    - `Joe.typeOf(opaqueValue).name()` is the `Class::getName` value for the 
      Java type.
  - Added the `CatchResult` type as the result of the `catch()` method,
    replacing the `Tuple` type.
  - Deleted the `Tuple` type, as it now seems ill-conceived.

## Changes in 0.5.0 

- Language
  - A default `toString()`  method is defined automatically for instances of 
    Joe classes.
    - And can be overridden by the class declaration.
    - It is always the case that `stringify(instance) == instance.toString()` 
      for instances of Joe classes.
  - Greatly improved `JoeError`'s stack trace output.  It now includes, for
    each stack level,
    - Complete function signatures, where available
    - Source file and line number, where available

- Bert Byte-code Engine
  - Added `BertEngine`, a byte-code execution engine, following
    Nystrom's `clox` design in Java.
  - `BertEngine` and `WalkerEngine` both implement the entire Joe language
    and support the entire Joe standard library, as verified by the 
    Joe test suites.
  - `BertEngine` is now enabled by default.
  - The `WalkerEngine` can enabled by the `-w` option on relevant `joe` tools.

- Extending/Embedding API
  - Added `Joe::isComplete`.
  - It is now possible to create bindings for native types that can be extended
    by scripted classes. 

- Library
  - Experimental `joe.win` Package
    - Added JavaFX widgets: `Menu`, `MenuBar`, `MenuItem`, `Separator`, 
      `Tab`, `TabPane`, `ListView`, `SplitPane`, `GridPane`
    - Added JavaFX enums: `Orientation`, `Side`

- Bugs fixed
  - Test script `print`/`println` output is now hidden unless `joe test` is
    run with the `--verbose` option.

## Changes in 0.4.0 

- Language
  - Added the `@` operator.
    - In class methods, `@name` is identical to `this.name`.

- Extending/Embedding API
  - It is now possible to define native types that more fully resemble 
    Joe classes and instances, e.g., the new `TextBuilder`.
    - Instances of `TextBuilder` have data fields, just like Joe class
      instances.
    - `TextBuilder` can be extended by Joe classes.
  - Moved the arity checking methods (e.g., `Joe.exactArity()`) from
    `Joe` to `Args`.

- Library
  - Added experimental `joe.win` package for creating JavaFX GUIs in Joe.
    - Optional package.
    - Loaded (with `joe.console`) by new `joe win` tool.
    - Widgets
      - `Node`, `Region`
      - `Pane`, `StackPane`, `VBox`, `HBox`
      - `Control`, `Label`, `Button`
  - Added `EnumProxy<E>` for implementing bindings to Java enums.
  - Added `Tuple` type, as a tuple return type for functions.
    - The standard `catch()` function now returns a `Tuple` rather than a
      `Pair`.
    - Removed `Pair`.
  - Replaced the `StringBuilder` type with the new `TextBuilder` type,
    which is a native type that can be subclassed by Joe classes.

- Tools
  - Experimental `joe win` tool
  - `joe doc`
    - Added `@enum` entity for documenting enum types.
    - Added `@mixin` entity for documentation to be included into
      multiple `@type` entities
  - `joe test`
    - Added `assertTrue`, `assertFalse`, `assertError`, and `skip` functions.
  - `joe version`
    - New tool, outputs the Joe version and build date.
     
- Miscellaneous
  - Removed `Joe::codify`.
  - Removed the various flavors of `Joe::recodify`.
  - Added Ant `build.xml` for building releases.

  
## Changes in 0.3.0 (2024-10-11)

- Language
  - Numeric literals
    - Can be entered using scientific notation, e.g., `12.34e56`
    - Can be entered as hex literals, e.g., `0x12FF`.
    - See [Types and Values: Numbers](types.md#numbers).
  
  - Text blocks
    - Normal strings can no longer contain unescaped newlines.
    - Joe now has Java-like text blocks, enclosed in `"""` pairs.
    - See [Types and Values: Strings](types.md#strings).
  
  - Raw string literals
    - Raw string literals are entered using single quotes rather than 
      double quotes.  Within a raw string literal the backslash character
      has no special meaning.
    - Raw text blocks are entered using `'''` rather than `"""`.

  - Operators
    - Added the `in` and `ni` [membership operators](operators.md#membership-operators).
    - Added the `+=`, `-=`, `*=`, and `/=` assignment operators, with
      their usual semantics.
    - Added the `++` and `--` operators, with their usual semantics.
  
  - Statements
    - Added the [`switch` statement](statements.md#switch-statements).
     
- Library
  - A value type's type proxy can now inherit methods from the value type's
    supertype's type proxy.
  - Added the [`Map`](library/type.joe.Map.md) type.
  - Added the [`Set`](library/type.joe.Set.md) type.
  - Added the [`AssertError`](library/type.joe.AssertError.md) type, which
    explicitly extends the [`Error`](library/type.joe.Error.md) type.
  - Added the [`StringBuilder`](library/type.joe.StringBuilder.md) type.
  - Added the optional [`joe.console`](library/pkg.joe.console.md) package,
    for use by scripts invoked by the command line, including the 
    [`Path`](library/type.joe.console.Path.md) type.
  - Implemented the `String.format()` method for formatting strings based on
    a format string, mostly mirroring Java's method of the same name. See 
    [String Formatting](library/type.joe.String.md#topic.formatting) for
    details.
  - Added `printf()` to the [Standard Library](library/pkg.joe.md).
  - Added a `printf()` method to the 
    [`StringBuilder`](library/type.joe.StringBuilder.md) type.

- `joe run` Tool
  - Installs the [`joe.console`](library/pkg.joe.console.md) package.
   
- `joe repl` Tool
  - Installs the [`joe.console`](library/pkg.joe.console.md) package.

- `joe doc` Tool
  - A `@type`'s doc comment can now reference the type's supertype using
    the `@extends` metadata tag.
  - `@package` and `@type` documentation can include additional topic sections
    at the bottom of their documentation pages using the new `@packageTopic`
    and `@typeTopic` entries.

- Documentation
  - Added the relevant documentation for all changes list above.
  - Added the topics "Stringification" and "Iterability" to the
    [Registered Types](extending/registered_types.md) section.

- Bug fixes
  - Fixed broken `println()` function.

## Changes in 0.2.0 (2024-09-27)

Joe 0.2.0 extends Joe on every axis.

- Documentation
  - Added this User's Guide
  
- Language
  - Added `assert` statement
  - Removed `print` statement (replaced with `println()`, etc.)
  - `+` will do string concatenation provided that at least one operand is
    a string.
  - Added `Keyword` values: `#abc` is an interned constant.
  - Added the ternary operator `? :`.
  - Joe `class` declarations may now include `static method` declarations
    and `static {...}` initializer blocks.  Static methods and variables are 
    always referenced by the class name, as in Java.
  - Joe functions and methods can now accept a variable length argument list 
    by defining an `args` parameter as the last parameter in the list.  `args`
    will be a list containing any excess values.
  - Added lambda functions, e.g., `\x -> x*x` or `\x -> { return x*x; }`.
  - Added `throw` statement.
  - Added `break` and `continue` statements.
  - Added `foreach` statement.
  - `JoeError` exceptions now include a script-level stack trace, populated as
    the stack is unwound.
  
- Library
  - Added `catch()` function
  - Added `compare()` function
  - Added `println()`, `print()` functions
  - Added `Error` proxy (for `JoeError` and its subclasses)
  - Added `Keyword` Java type, with proxy
  - Added `List` Java type, with proxy
  - Added `Number` proxy (all numbers are Java `Doubles`)
  - Added `Pair` Java type, with proxy
  - Added `String` proxy
    
- Embedding API
  - Cleaned up top-level error handling
  - Added `Joe::installScriptResource`
  - Added `TypeProxy<V>`, a class that defines a binding for native Java
    type `V`.  A `TypeProxy` can provide
    - Instance methods and an initializer for type V
      - E.g., a `String`'s `length()` method.
    - Static methods and constants
      - E.g., `Double.PI`, `Double.abs()`.
  - Added notion of a `JoePackage`, which defines some number of global
    functions and type proxies.
  - All script-level output now goes through `Joe::setOutputHandler`,
    which defaults to writing to `System.out`.
    - The methods `Joe::println` and `Joe::print` can be used from 
      Java bindings.

- Tools
  - Added `joe test`, a test runner for Joe-level test scripts, with a aPackage
    of assertion checkers.
  - Added `joe doc`, a tool var producing Joe API documentation from 
    scanned "JoeDoc" comments.  (See the 
    [Library API](library.md) for examples of `joe doc` output.)

- Development
  - Added `joe/tests`, the script-level Joe test suite.

- Misc
  - Added LICENSE and CONTRIBUTING.md

## Joe 0.1.0 (2024/09/01)

Joe 0.1.0 is a complete implementation of JLox, with the following changes:

- Syntax
    - `null` replaces `nil`.
    - `&&` replaces `and`.
    - `||` replaces `or`.
    - Strings can include the standard string escapes, which are converted into the usual characters.
        - '\\\\', `\t`, `\b`, `\n`, `\r`, `\f`, `\"`
    - `function` replaces `fun` as the function definition keyword.
    - `method` is added as the method definition keyword.
    - The `extends` keyword replaces `<` in `class Sub extends Super {...}`.
- Semantics
    - The `<`, `<=`, `>`, and `>=` operators can compare strings.
    - A function with no `return` returns the value of the last statement in the
      block (which is usually null).
    - An expression statement yields the value of the expression.
    - Thus, running a script returns a value.
- Embedding API
    - The `Joe` engine is separated from the `App` application, so that a client
      can create `Joe` instances as needed.
        - The embedding API is still mostly non-existent.
- Tools
    - The `App` includes "tool" infrastructure, and supports two tools, accessed
      as `joe run` and `joe repl`.
        - The intent is that a client project can reuse the underlying tools in its
          own application if desired.
        - `joe repl` outputs the value of each statement; other than that, the
          two tools are more or less as described in *Crafting Interpreters*.

