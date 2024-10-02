# Change Log

## Changes in 0.3.0 (Forthcoming)

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
     
- Library
  - A value type's type proxy can now inherit methods from the value type's
    supertype's type proxy.
  - Added the [`Map`](library/type.joe.Map.md) type.
  - Added the [`AssertError`](library/type.joe.AssertError.md) type, which
    explicitly extends the [`Error`](library/type.joe.Error.md) type.
  - Added the [`StringBuilder`](library/type.joe.StringBuilder.md) type.

- `joe doc` Tool
  - A `@type`'s doc comment can now reference the type's supertype using
    the `@extends` metadata tag.

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

